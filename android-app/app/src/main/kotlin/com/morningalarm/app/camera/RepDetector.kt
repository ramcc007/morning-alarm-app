package com.morningalarm.app.camera

import com.google.mlkit.vision.pose.Pose

/**
 * Common interface for a camera-driven exercise rep counter. Implement this
 * for each new `ExerciseType` (squats, jumping jacks, ...) — see
 * [PushupRepDetector] as the reference implementation.
 */
interface RepDetector {
    /** Number of valid reps counted so far in this session. */
    val repCount: Int

    /** Human readable hint describing what's blocking rep detection right now, or null when tracking is good. */
    val guidance: String?

    /** Called every time a new valid rep completes, with the running total. */
    var onRepCompleted: ((Int) -> Unit)?

    /** Feed one ML Kit pose result (already run on the current camera frame). */
    fun process(pose: Pose)

    /** Reset the counter, e.g. when the ringing screen reappears for a fresh attempt. */
    fun reset()
}
