package com.wakerep.app.camera

import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Counts squat reps by tracking the hip-knee-ankle angle — the same
 * down -> up transition-counting approach as [PushupRepDetector], just
 * applied to the legs instead of the arms.
 */
class SquatRepDetector : JointAngleRepDetector(
    downThresholdDegrees = 100.0,
    upThresholdDegrees = 160.0,
    proximalLeft = PoseLandmark.LEFT_HIP,
    middleLeft = PoseLandmark.LEFT_KNEE,
    distalLeft = PoseLandmark.LEFT_ANKLE,
    proximalRight = PoseLandmark.RIGHT_HIP,
    middleRight = PoseLandmark.RIGHT_KNEE,
    distalRight = PoseLandmark.RIGHT_ANKLE,
    insufficientVisibilityMessage = "Step back so your hips, knees and ankles are all visible",
)
