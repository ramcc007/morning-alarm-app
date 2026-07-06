import Foundation

/// A physical exercise the app can validate via the camera before an alarm can be dismissed.
///
/// New exercises plug in by adding a case here and a matching `RepDetector`
/// implementation (see `PushupDetector` for the reference implementation).
enum ExerciseType: String, Codable, CaseIterable, Identifiable {
    case pushups

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .pushups: return "Push-ups"
        }
    }

    var instructions: String {
        switch self {
        case .pushups:
            return "Prop your phone up so the front camera can see your full upper body, then start doing push-ups. The alarm stops once you've completed your target reps with good form."
        }
    }

    var iconSystemName: String {
        switch self {
        case .pushups: return "figure.strengthtraining.traditional"
        }
    }

    /// Sensible default rep target for this exercise.
    var defaultTarget: Int {
        switch self {
        case .pushups: return 3
        }
    }

    /// Allowed range a user can configure for the rep target.
    var targetRange: ClosedRange<Int> {
        switch self {
        case .pushups: return 1...50
        }
    }
}
