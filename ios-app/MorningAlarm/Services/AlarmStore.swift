import Foundation
import Combine

/// Source of truth for the user's alarms. Persists to disk as JSON and keeps
/// scheduled local notifications in sync whenever alarms change.
@MainActor
final class AlarmStore: ObservableObject {
    @Published private(set) var alarms: [Alarm] = []

    private let fileURL: URL
    private let notificationManager: NotificationManager

    init(notificationManager: NotificationManager = .shared) {
        self.notificationManager = notificationManager
        let documents = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        self.fileURL = documents.appendingPathComponent("alarms.json")
        load()
    }

    func add(_ alarm: Alarm) {
        alarms.append(alarm)
        alarms.sort { ($0.hour, $0.minute) < ($1.hour, $1.minute) }
        notificationManager.schedule(alarm)
        save()
    }

    func update(_ alarm: Alarm) {
        guard let index = alarms.firstIndex(where: { $0.id == alarm.id }) else { return }
        alarms[index] = alarm
        notificationManager.schedule(alarm)
        save()
    }

    func delete(at offsets: IndexSet) {
        for index in offsets {
            notificationManager.cancel(alarms[index])
        }
        alarms.remove(atOffsets: offsets)
        save()
    }

    func delete(_ alarm: Alarm) {
        notificationManager.cancel(alarm)
        alarms.removeAll { $0.id == alarm.id }
        save()
    }

    func setEnabled(_ alarm: Alarm, enabled: Bool) {
        guard let index = alarms.firstIndex(where: { $0.id == alarm.id }) else { return }
        alarms[index].isEnabled = enabled
        if enabled {
            notificationManager.schedule(alarms[index])
        } else {
            notificationManager.cancel(alarms[index])
        }
        save()
    }

    func alarm(withId id: UUID) -> Alarm? {
        alarms.first { $0.id == id }
    }

    private func save() {
        do {
            let data = try JSONEncoder().encode(alarms)
            try data.write(to: fileURL, options: .atomic)
        } catch {
            print("AlarmStore: failed to save alarms - \(error)")
        }
    }

    private func load() {
        guard let data = try? Data(contentsOf: fileURL) else { return }
        do {
            alarms = try JSONDecoder().decode([Alarm].self, from: data)
        } catch {
            print("AlarmStore: failed to load alarms - \(error)")
        }
    }
}
