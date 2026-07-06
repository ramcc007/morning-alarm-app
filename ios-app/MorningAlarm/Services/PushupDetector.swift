import Vision
import CoreGraphics

/// Counts push-up reps from a stream of `VNHumanBodyPoseObservation`s.
///
/// Approach: track the elbow angle (shoulder-elbow-wrist) on whichever arm(s)
/// are visible with sufficient confidence. A rep is counted on the
/// down -> up transition, so a half push-up (going down but never fully
/// extending back up) never counts, and the alarm can't be cheated by
/// wiggling an arm near full extension.
///
/// This intentionally does not attempt full 3D form analysis (Vision's
/// body-pose points are 2D image-space) - it's a pragmatic, on-device,
/// real-time heuristic, the same tradeoff most consumer fitness apps make.
final class PushupDetector: RepDetector {
    /// Arm considered "down" (flexed) once its elbow angle drops below this many degrees.
    private let downThresholdDegrees: CGFloat = 95
    /// Arm considered "up" (extended) once its elbow angle rises above this many degrees.
    private let upThresholdDegrees: CGFloat = 155
    /// Minimum Vision confidence for a joint to be trusted.
    private let minPointConfidence: Float = 0.3
    /// Minimum time between counted reps, guards against jitter double-counting.
    private let minRepInterval: TimeInterval = 0.5

    private enum ArmState {
        case up
        case down
        case unknown
    }

    private var state: ArmState = .unknown
    private var lastRepDate: Date = .distantPast

    private(set) var repCount: Int = 0 {
        didSet { onRepCompleted?(repCount) }
    }
    var onRepCompleted: ((Int) -> Void)?
    private(set) var currentGuidance: String?

    func reset() {
        repCount = 0
        state = .unknown
        lastRepDate = .distantPast
        currentGuidance = nil
    }

    func process(observation: VNHumanBodyPoseObservation) {
        guard let angle = averageElbowAngle(observation) else {
            return // guidance already set by averageElbowAngle
        }
        currentGuidance = nil

        switch state {
        case .unknown:
            state = angle >= upThresholdDegrees ? .up : (angle <= downThresholdDegrees ? .down : .unknown)

        case .up:
            if angle <= downThresholdDegrees {
                state = .down
            }

        case .down:
            if angle >= upThresholdDegrees {
                state = .up
                let now = Date()
                if now.timeIntervalSince(lastRepDate) >= minRepInterval {
                    lastRepDate = now
                    repCount += 1
                }
            }
        }
    }

    /// Returns the average elbow angle (in degrees) across whichever arm(s)
    /// are visible with enough confidence, or nil (with `currentGuidance` set)
    /// if not enough of the body is visible to trust a measurement.
    private func averageElbowAngle(_ observation: VNHumanBodyPoseObservation) -> CGFloat? {
        var angles: [CGFloat] = []

        if let angle = elbowAngle(observation, shoulder: .leftShoulder, elbow: .leftElbow, wrist: .leftWrist) {
            angles.append(angle)
        }
        if let angle = elbowAngle(observation, shoulder: .rightShoulder, elbow: .rightElbow, wrist: .rightWrist) {
            angles.append(angle)
        }

        guard !angles.isEmpty else {
            currentGuidance = "Move back so both shoulders, elbows and wrists are visible"
            return nil
        }

        return angles.reduce(0, +) / CGFloat(angles.count)
    }

    private func elbowAngle(
        _ observation: VNHumanBodyPoseObservation,
        shoulder: VNHumanBodyPoseObservation.JointName,
        elbow: VNHumanBodyPoseObservation.JointName,
        wrist: VNHumanBodyPoseObservation.JointName
    ) -> CGFloat? {
        guard
            let shoulderPoint = try? observation.recognizedPoint(shoulder),
            let elbowPoint = try? observation.recognizedPoint(elbow),
            let wristPoint = try? observation.recognizedPoint(wrist),
            shoulderPoint.confidence >= minPointConfidence,
            elbowPoint.confidence >= minPointConfidence,
            wristPoint.confidence >= minPointConfidence
        else { return nil }

        let vectorToShoulder = CGPoint(x: shoulderPoint.location.x - elbowPoint.location.x,
                                        y: shoulderPoint.location.y - elbowPoint.location.y)
        let vectorToWrist = CGPoint(x: wristPoint.location.x - elbowPoint.location.x,
                                     y: wristPoint.location.y - elbowPoint.location.y)

        let dot = vectorToShoulder.x * vectorToWrist.x + vectorToShoulder.y * vectorToWrist.y
        let magnitudeA = sqrt(vectorToShoulder.x * vectorToShoulder.x + vectorToShoulder.y * vectorToShoulder.y)
        let magnitudeB = sqrt(vectorToWrist.x * vectorToWrist.x + vectorToWrist.y * vectorToWrist.y)
        guard magnitudeA > 0, magnitudeB > 0 else { return nil }

        let cosAngle = max(-1, min(1, dot / (magnitudeA * magnitudeB)))
        return acos(cosAngle) * 180 / .pi
    }
}
