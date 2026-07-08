package com.wakerep.app.camera

import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Counts push-up reps from a stream of ML Kit [Pose] results.
 *
 * Approach: track the elbow angle (shoulder-elbow-wrist) on whichever arm(s)
 * are visible with sufficient confidence. A rep is counted on the
 * down -> up transition, so a half push-up (going down but never fully
 * extending back up) never counts, and the alarm can't be cheated by
 * wiggling an arm near full extension.
 *
 * This is a pragmatic 2D/near-3D heuristic (ML Kit's pose landmarks carry an
 * approximate z, but it's not a true depth sensor) - the same tradeoff most
 * consumer fitness apps make on a single RGB camera.
 */
class PushupRepDetector : RepDetector {

    private val downThresholdDegrees = 95.0
    private val upThresholdDegrees = 155.0
    private val minLandmarkConfidence = 0.3f
    private val minRepIntervalMs = 500L

    private enum class ArmState { UP, DOWN, UNKNOWN }

    private var state = ArmState.UNKNOWN
    private var lastRepAt = 0L

    override var repCount: Int = 0
        private set

    override var onRepCompleted: ((Int) -> Unit)? = null
    override var guidance: String? = null
        private set

    override fun reset() {
        repCount = 0
        state = ArmState.UNKNOWN
        lastRepAt = 0L
        guidance = null
    }

    override fun process(pose: Pose) {
        val angle = averageElbowAngle(pose) ?: return // guidance already set by averageElbowAngle
        guidance = null

        when (state) {
            ArmState.UNKNOWN -> {
                state = when {
                    angle >= upThresholdDegrees -> ArmState.UP
                    angle <= downThresholdDegrees -> ArmState.DOWN
                    else -> ArmState.UNKNOWN
                }
            }
            ArmState.UP -> {
                if (angle <= downThresholdDegrees) state = ArmState.DOWN
            }
            ArmState.DOWN -> {
                if (angle >= upThresholdDegrees) {
                    state = ArmState.UP
                    val now = System.currentTimeMillis()
                    if (now - lastRepAt >= minRepIntervalMs) {
                        lastRepAt = now
                        repCount += 1
                        onRepCompleted?.invoke(repCount)
                    }
                }
            }
        }
    }

    private fun averageElbowAngle(pose: Pose): Double? {
        val angles = listOfNotNull(
            elbowAngle(pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),
            elbowAngle(pose, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),
        )
        if (angles.isEmpty()) {
            guidance = "Move back so both shoulders, elbows and wrists are visible"
            return null
        }
        return angles.average()
    }

    private fun elbowAngle(pose: Pose, shoulderType: Int, elbowType: Int, wristType: Int): Double? {
        val shoulder = pose.getPoseLandmark(shoulderType) ?: return null
        val elbow = pose.getPoseLandmark(elbowType) ?: return null
        val wrist = pose.getPoseLandmark(wristType) ?: return null

        if (shoulder.inFrameLikelihood < minLandmarkConfidence ||
            elbow.inFrameLikelihood < minLandmarkConfidence ||
            wrist.inFrameLikelihood < minLandmarkConfidence
        ) return null

        return angleBetween(shoulder.position3D, elbow.position3D, wrist.position3D)
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
