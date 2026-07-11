package com.wakerep.app.camera

import com.google.mlkit.vision.pose.Pose

/**
 * Common interface for a camera-driven exercise rep counter. Implement this
 * for each new `ExerciseType` (squats, jumping jacks, ...) — see
 * [PushupRepDetector] as the reference implementation.
 */
interface RepDetector {
    /** Number of valid reps counted so far in this session. */
    val repCount: Int

    /**
     * Continuous 0f..1f progress of the *current, not-yet-counted* rep -
     * 0 at the bottom/closed position, 1 at the top/open position. Updated
     * every frame, independent of [repCount]. This is what lets the ringing
     * screen's horizon bind to real-time body position rather than only
     * jumping once per completed rep - see the design system's "the light
     * is spatially bound to your body" interaction.
     */
    val liveExtension: Float

    /** Human readable hint describing what's blocking rep detection right now, or null when tracking is good. */
    val guidance: String?

    /** Called every time a new valid rep completes, with the running total. */
    var onRepCompleted: ((Int) -> Unit)?

    /** Called after every processed frame, whether or not a rep completed, so callers can read fresh state each frame. */
    var onLiveUpdate: (() -> Unit)?

    /** Feed one ML Kit pose result (already run on the current camera frame). */
    fun process(pose: Pose)

    /** Reset the counter, e.g. when the ringing screen reappears for a fresh attempt. */
    fun reset()
}
