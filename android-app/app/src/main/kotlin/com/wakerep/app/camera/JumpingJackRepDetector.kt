package com.wakerep.app.camera

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs

/**
 * Counts jumping-jack reps. Unlike push-ups/squats this isn't a single
 * joint angle - it's a whole-body "open" (arms overhead, legs spread) vs
 * "closed" (arms down, legs together) posture, so it implements
 * [RepDetector] directly rather than extending [JointAngleRepDetector].
 *
 * Both signals (arms up AND legs apart) are required together for a frame
 * to count as "open," which avoids false reps from just waving an arm.
 * Thresholds are scaled by the person's own torso length / hip width so
 * detection stays consistent regardless of distance from the camera.
 */
class JumpingJackRepDetector : RepDetector {

    private val minLandmarkConfidence = 0.3f
    private val minRepIntervalMs = 400L
    private val armsUpMarginRatio = 0.15
    private val legSpreadRatio = 1.6

    private enum class JackState { OPEN, CLOSED, UNKNOWN }
    private var state = JackState.UNKNOWN
    private var lastRepAt = 0L

    override var repCount: Int = 0
        private set
    override var liveExtension: Float = 0f
        private set
    override var onRepCompleted: ((Int) -> Unit)? = null
    override var onLiveUpdate: (() -> Unit)? = null
    override var guidance: String? = null
        private set

    override fun reset() {
        repCount = 0
        liveExtension = 0f
        state = JackState.UNKNOWN
        lastRepAt = 0L
        guidance = null
    }

    override fun process(pose: Pose) {
        val isOpen = classifyPosture(pose)
        if (isOpen == null) {
            onLiveUpdate?.invoke() // guidance already set by classifyPosture
            return
        }
        guidance = null

        when (state) {
            JackState.UNKNOWN -> state = if (isOpen) JackState.OPEN else JackState.CLOSED
            JackState.CLOSED -> if (isOpen) state = JackState.OPEN
            JackState.OPEN -> if (!isOpen) {
                state = JackState.CLOSED
                val now = System.currentTimeMillis()
                if (now - lastRepAt >= minRepIntervalMs) {
                    lastRepAt = now
                    repCount += 1
                    onRepCompleted?.invoke(repCount)
                }
            }
        }

        onLiveUpdate?.invoke()
    }

    /** True = arms overhead + legs spread ("open"), false = "closed", null = not enough of the body visible. */
    private fun classifyPosture(pose: Pose): Boolean? {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        val landmarks = listOf(leftShoulder, rightShoulder, leftWrist, rightWrist, leftHip, rightHip, leftAnkle, rightAnkle)
        if (landmarks.any { it == null || it.inFrameLikelihood < minLandmarkConfidence }) {
            guidance = "Step back so your whole body - arms and legs - is visible"
            return null
        }

        val shoulderMidY = (leftShoulder!!.position.y + rightShoulder!!.position.y) / 2f
        val hipMidY = (leftHip!!.position.y + rightHip!!.position.y) / 2f
        val torsoLength = abs(hipMidY - shoulderMidY).coerceAtLeast(1f)

        val minWristY = minOf(leftWrist!!.position.y, rightWrist!!.position.y)
        val armsUp = leftWrist.position.y < shoulderMidY - torsoLength * armsUpMarginRatio &&
            rightWrist.position.y < shoulderMidY - torsoLength * armsUpMarginRatio

        val hipWidth = abs(leftHip.position.x - rightHip.position.x).coerceAtLeast(1f)
        val ankleSpread = abs(leftAnkle!!.position.x - rightAnkle!!.position.x)
        val legsApart = ankleSpread > hipWidth * legSpreadRatio

        // Continuous approximation blending arm-raise and leg-spread progress, purely for the
        // live visual feedback (the OPEN/CLOSED booleans above still drive actual rep counting).
        val armProgress = (((shoulderMidY - minWristY) / torsoLength) / (armsUpMarginRatio * 3f))
            .coerceIn(0f, 1f)
        val legProgress = ((ankleSpread / hipWidth) / (legSpreadRatio * 1.2f)).coerceIn(0f, 1f)
        liveExtension = (armProgress + legProgress) / 2f

        return armsUp && legsApart
    }
}
