package com.wakerep.app.camera

import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Counts push-up reps by tracking the shoulder-elbow-wrist angle. A rep is
 * counted on the down -> up transition, so a half push-up (going down but
 * never fully extending back up) never counts, and the alarm can't be
 * cheated by wiggling an arm near full extension.
 */
class PushupRepDetector : JointAngleRepDetector(
    downThresholdDegrees = 95.0,
    upThresholdDegrees = 155.0,
    proximalLeft = PoseLandmark.LEFT_SHOULDER,
    middleLeft = PoseLandmark.LEFT_ELBOW,
    distalLeft = PoseLandmark.LEFT_WRIST,
    proximalRight = PoseLandmark.RIGHT_SHOULDER,
    middleRight = PoseLandmark.RIGHT_ELBOW,
    distalRight = PoseLandmark.RIGHT_WRIST,
    insufficientVisibilityMessage = "Move back so both shoulders, elbows and wrists are visible",
)
