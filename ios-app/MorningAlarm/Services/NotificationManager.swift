import Foundation
import UserNotifications

/// Thin wrapper around `UNUserNotificationCenter` for requesting permission and
/// scheduling/cancelling the local notifications that back each `Alarm`.
final class NotificationManager {
    static let shared = NotificationManager()

    static let categoryIdentifier = "ALARM_CATEGORY"
    /// Key used in notification userInfo to carry the triggering alarm's id back to the app.
    static let alarmIdKey = "alarmId"

    private init() {}

    func requestAuthorization() async -> Bool {
        let center = UNUserNotificationCenter.current()
        do {
            let granted = try await center.requestAuthorization(options: [.alert, .sound, .badge])
            registerCategories()
            return granted
        } catch {
            return false
        }
    }

    func registerCategories() {
        let category = UNNotificationCategory(
            identifier: Self.categoryIdentifier,
            actions: [],
            intentIdentifiers: [],
            options: [.customDismissAction]
        )
        UNUserNotificationCenter.current().setNotificationCategories([category])
    }

    /// Schedules (or re-schedules) all notifications for the given alarm.
    func schedule(_ alarm: Alarm) {
        cancel(alarm)
        guard alarm.isEnabled else { return }

        let center = UNUserNotificationCenter.current()
        let content = makeContent(for: alarm)

        if alarm.repeatDays.isEmpty {
            var dateComponents = Calendar.current.dateComponents([.year, .month, .day], from: nextOccurrence(for: alarm))
            dateComponents.hour = alarm.hour
            dateComponents.minute = alarm.minute
            let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: false)
            let request = UNNotificationRequest(identifier: "\(alarm.id.uuidString)-once", content: content, trigger: trigger)
            center.add(request)
        } else {
            for weekday in Weekday.ordered.map(\.0) where alarm.repeatDays.contains(weekday) {
                guard let calDay = weekday.calendarWeekday else { continue }
                var dateComponents = DateComponents()
                dateComponents.hour = alarm.hour
                dateComponents.minute = alarm.minute
                dateComponents.weekday = calDay
                let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
                let request = UNNotificationRequest(identifier: "\(alarm.id.uuidString)-\(calDay)", content: content, trigger: trigger)
                center.add(request)
            }
        }
    }

    /// Schedules a one-shot notification `alarm.snoozeMinutes` from now,
    /// without touching the alarm's regular recurring schedule.
    func scheduleSnooze(for alarm: Alarm) {
        let center = UNUserNotificationCenter.current()
        let content = makeContent(for: alarm)
        content.title = "\(alarm.label) (Snoozed)"
        let trigger = UNTimeIntervalNotificationTrigger(
            timeInterval: TimeInterval(alarm.snoozeMinutes * 60),
            repeats: false
        )
        let request = UNNotificationRequest(
            identifier: "\(alarm.id.uuidString)-snooze-\(UUID().uuidString)",
            content: content,
            trigger: trigger
        )
        center.add(request)
    }

    func cancel(_ alarm: Alarm) {
        let allPossibleIds = ["\(alarm.id.uuidString)-once"] + (1...7).map { "\(alarm.id.uuidString)-\($0)" }
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: allPossibleIds)
    }

    private func makeContent(for alarm: Alarm) -> UNMutableNotificationContent {
        let content = UNMutableNotificationContent()
        content.title = alarm.label
        content.body = "Do \(alarm.repTarget) \(alarm.exercise.displayName.lowercased()) to stop the alarm."
        content.sound = UNNotificationSound(named: UNNotificationSoundName("\(alarm.soundName).caf"))
        content.categoryIdentifier = Self.categoryIdentifier
        content.userInfo = [Self.alarmIdKey: alarm.id.uuidString]
        content.interruptionLevel = .timeSensitive
        return content
    }

    private func nextOccurrence(for alarm: Alarm) -> Date {
        let calendar = Calendar.current
        let now = Date()
        var candidate = calendar.date(bySettingHour: alarm.hour, minute: alarm.minute, second: 0, of: now) ?? now
        if candidate <= now {
            candidate = calendar.date(byAdding: .day, value: 1, to: candidate) ?? candidate
        }
        return candidate
    }
}
