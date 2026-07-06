import Foundation

/// Day-of-week flags for repeating alarms. Matches `Calendar` weekday numbering (1 = Sunday).
struct Weekday: OptionSet, Codable, Hashable {
    let rawValue: Int

    static let sunday    = Weekday(rawValue: 1 << 0)
    static let monday    = Weekday(rawValue: 1 << 1)
    static let tuesday   = Weekday(rawValue: 1 << 2)
    static let wednesday = Weekday(rawValue: 1 << 3)
    static let thursday  = Weekday(rawValue: 1 << 4)
    static let friday    = Weekday(rawValue: 1 << 5)
    static let saturday  = Weekday(rawValue: 1 << 6)

    static let everyday: Weekday = [.sunday, .monday, .tuesday, .wednesday, .thursday, .friday, .saturday]
    static let weekdays: Weekday = [.monday, .tuesday, .wednesday, .thursday, .friday]
    static let weekend: Weekday = [.sunday, .saturday]

    static let ordered: [(Weekday, String)] = [
        (.sunday, "S"), (.monday, "M"), (.tuesday, "T"), (.wednesday, "W"),
        (.thursday, "T"), (.friday, "F"), (.saturday, "S")
    ]

    /// Calendar `weekday` value (1...7) this option corresponds to, if it represents a single day.
    var calendarWeekday: Int? {
        switch self {
        case .sunday: return 1
        case .monday: return 2
        case .tuesday: return 3
        case .wednesday: return 4
        case .thursday: return 5
        case .friday: return 6
        case .saturday: return 7
        default: return nil
        }
    }

    var summary: String {
        if self == .everyday { return "Every day" }
        if self == .weekdays { return "Weekdays" }
        if self == .weekend { return "Weekends" }
        if isEmpty { return "Once" }
        let names: [(Weekday, String)] = [
            (.sunday, "Sun"), (.monday, "Mon"), (.tuesday, "Tue"), (.wednesday, "Wed"),
            (.thursday, "Thu"), (.friday, "Fri"), (.saturday, "Sat")
        ]
        return names.filter { contains($0.0) }.map(\.1).joined(separator: ", ")
    }
}

struct Alarm: Identifiable, Codable, Equatable {
    let id: UUID
    var label: String
    var hour: Int
    var minute: Int
    var repeatDays: Weekday
    var isEnabled: Bool
    var exercise: ExerciseType
    var repTarget: Int
    var soundName: String
    var snoozeEnabled: Bool
    var snoozeMinutes: Int

    init(
        id: UUID = UUID(),
        label: String = "Alarm",
        hour: Int,
        minute: Int,
        repeatDays: Weekday = [],
        isEnabled: Bool = true,
        exercise: ExerciseType = .pushups,
        repTarget: Int? = nil,
        soundName: String = "classic_alarm",
        snoozeEnabled: Bool = true,
        snoozeMinutes: Int = 5
    ) {
        self.id = id
        self.label = label
        self.hour = hour
        self.minute = minute
        self.repeatDays = repeatDays
        self.isEnabled = isEnabled
        self.exercise = exercise
        self.repTarget = repTarget ?? exercise.defaultTarget
        self.soundName = soundName
        self.snoozeEnabled = snoozeEnabled
        self.snoozeMinutes = snoozeMinutes
    }

    var time: Date {
        Calendar.current.date(bySettingHour: hour, minute: minute, second: 0, of: Date()) ?? Date()
    }

    var timeString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "h:mm a"
        return formatter.string(from: time)
    }

    var repeatSummary: String { repeatDays.summary }

    /// Notification identifiers for this alarm, one per active weekday (or a single one-shot id).
    func notificationIdentifiers() -> [String] {
        if repeatDays.isEmpty {
            return ["\(id.uuidString)-once"]
        }
        return Weekday.ordered.compactMap { weekday, _ in
            guard repeatDays.contains(weekday), let calDay = weekday.calendarWeekday else { return nil }
            return "\(id.uuidString)-\(calDay)"
        }
    }
}
