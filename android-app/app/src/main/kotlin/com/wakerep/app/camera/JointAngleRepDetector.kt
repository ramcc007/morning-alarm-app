package com.wakerep.app.camera

import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.pose.Pose
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Shared rep-counting engine for any exercise that reduces to "track the
 * angle at a middle joint (elbow, knee, ...) across a left/right limb pair
 * and count a rep on each down -> up transition." Push-ups
 * (shoulder-elbow-wrist) and squats (hip-knee-ankle) both use this.
 *
 * Not every exercise fits this shape - jumping jacks, for example, is a
 * whole-body spread/close motion rather than a single joint angle, so it
 * implements [RepDetector] directly instead (see [JumpingJackRepDetector]).
 */
abstract class JointAngleRepDetector(
    private val downThresholdDegrees: Double,
    private val upThresholdDegrees: Double,
    private val proximalLeft: Int,
    private val middleLeft: Int,
    private val distalLeft: Int,
    private val proximalRight: Int,
    private val middleRight: Int,
    private val distalRight: Int,
    private val insufficientVisibilityMessage: String,
) : RepDetector {

    private val minLandmarkConfidence = 0.3f
    private val minRepIntervalMs = 500L

    /** How far below the up-threshold still counts as "attempting a descent" for shallow-rep detection. */
    private val shallowAttemptMarginDegrees = 20.0
    private val formWarningDurationMs = 1500L

    private enum class LimbState { UP, DOWN, UNKNOWN }
    private var state = LimbState.UNKNOWN
    private var lastRepAt = 0L
    private var isDescending = false
    private var formWarningExpiresAt = 0L

    final override var repCount: Int = 0
        private set
    final override var liveExtension: Float = 0f
        private set
    final override var onRepCompleted: ((Int) -> Unit)? = null
    final override var onLiveUpdate: (() -> Unit)? = null
    final override var guidance: String? = null
        private set

    final override fun reset() {
        repCount = 0
        liveExtension = 0f
        state = LimbState.UNKNOWN
        lastRepAt = 0L
        isDescending = false
        formWarningExpiresAt = 0L
        guidance = null
    }

    final override fun process(pose: Pose) {
        val angle = averageJointAngle(pose)
        if (angle == null) {
            onLiveUpdate?.invoke() // guidance already set by averageJointAngle
            return
        }
        guidance = null
        liveExtension = ((angle - downThresholdDegrees) / (upThresholdDegrees - downThresholdDegrees))
            .coerceIn(0.0, 1.0)
            .toFloat()

        when (state) {
            LimbState.UNKNOWN -> state = when {
                angle >= upThresholdDegrees -> LimbState.UP
                angle <= downThresholdDegrees -> LimbState.DOWN
                else -> LimbState.UNKNOWN
            }
            LimbState.UP -> when {
                angle <= downThresholdDegrees -> {
                    state = LimbState.DOWN
                    isDescending = false
                }
                angle <= upThresholdDegrees - shallowAttemptMarginDegrees -> {
                    isDescending = true
                }
                isDescending && angle >= upThresholdDegrees - 5.0 -> {
                    // Came back up without ever reaching full depth - a shallow, non-counting attempt.
                    isDescending = false
                    formWarningExpiresAt = System.currentTimeMillis() + formWarningDurationMs
                }
            }
            LimbState.DOWN -> if (angle >= upThresholdDegrees) {
                state = LimbState.UP
                val now = System.currentTimeMillis()
                if (now - lastRepAt >= minRepIntervalMs) {
                    lastRepAt = now
                    repCount += 1
                    onRepCompleted?.invoke(repCount)
                }
            }
        }

        if (System.currentTimeMillis() < formWarningExpiresAt) {
            guidance = "Go lower for it to count"
        }

        onLiveUpdate?.invoke()
    }

    private fun averageJointAngle(pose: Pose): Double? {
        val angles = listOfNotNull(
            jointAngle(pose, proximalLeft, middleLeft, distalLeft),
            jointAngle(pose, proximalRight, middleRight, distalRight),
        )
        if (angles.isEmpty()) {
            guidance = insufficientVisibilityMessage
            return null
        }
        return angles.average()
    }

    private fun jointAngle(pose: Pose, proximalType: Int, middleType: Int, distalType: Int): Double? {
        val proximal = pose.getPoseLandmark(proximalType) ?: return null
        val middle = pose.getPoseLandmark(middleType) ?: return null
        val distal = pose.getPoseLandmark(distalType) ?: return null

        if (proximal.inFrameLikelihood < minLandmarkConfidence ||
            middle.inFrameLikelihood < minLandmarkConfidence ||
            distal.inFrameLikelihood < minLandmarkConfidence
        ) return null

        return angleBetween(proximal.position3D, middle.position3D, distal.position3D)
    }

    /** Angle at [vertex] between rays to [a] and [b], in degrees. */
    private fun angleBetween(a: PointF3D, vertex: PointF3D, b: PointF3D): Double {
        val v1x = (a.x - vertex.x).toDouble()
        val v1y = (a.y - vertex.y).toDouble()
        val v2x = (b.x - vertex.x).toDouble()
        val v2y = (b.y - vertex.y).toDouble()

        val dot = v1x * v2x + v1y * v2y
        val mag1 = sqrt(v1x * v1x + v1y * v1y)
        val mag2 = sqrt(v2x * v2x + v2y * v2y)
        if (mag1 == 0.0 || mag2 == 0.0) return 180.0

        val cosAngle = (dot / (mag1 * mag2)).coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(cosAngle))
    }
}
