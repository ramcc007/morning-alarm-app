import Vision

/// Common interface for a camera-driven exercise rep counter. Implement this
/// for each new `ExerciseType` (squats, jumping jacks, ...) and wire it up in
/// `PushupCameraView` / wherever detectors are instantiated.
protocol RepDetector: AnyObject {
    /// Number of valid reps counted so far in this session.
    var repCount: Int { get }

    /// Called on the main actor every time a new valid rep completes, with the running total.
    var onRepCompleted: ((Int) -> Void)? { get set }

    /// Feed one Vision body-pose observation (already run on the current camera frame).
    func process(observation: VNHumanBodyPoseObservation)

    /// Human readable hint describing what's blocking rep detection right now
    /// (e.g. "Move back so your whole body is visible"), or nil when tracking is good.
    var currentGuidance: String? { get }

    /// Reset the counter, e.g. when the ringing screen reappears for a fresh attempt.
    func reset()
}
