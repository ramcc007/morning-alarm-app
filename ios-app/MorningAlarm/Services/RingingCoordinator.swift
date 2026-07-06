import Foundation
import Combine

/// Tracks which alarm (if any) is currently demanding the full-screen
/// "prove you did your exercise" flow. `AppDelegate` feeds this from
/// notification callbacks; `RootView` observes it to present `AlarmRingingView`.
@MainActor
final class RingingCoordinator: ObservableObject {
    static let shared = RingingCoordinator()

    @Published var activeAlarmId: UUID?

    private init() {}

    func trigger(alarmId: UUID) {
        activeAlarmId = alarmId
    }

    func clear() {
        activeAlarmId = nil
    }
}
