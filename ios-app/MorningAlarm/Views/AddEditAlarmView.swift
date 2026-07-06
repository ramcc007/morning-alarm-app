import SwiftUI

struct AddEditAlarmView: View {
    @Environment(\.dismiss) private var dismiss

    let existingAlarm: Alarm?
    let onSave: (Alarm) -> Void

    @State private var time: Date
    @State private var label: String
    @State private var repeatDays: Weekday
    @State private var exercise: ExerciseType
    @State private var repTarget: Int
    @State private var snoozeEnabled: Bool

    init(alarm: Alarm?, onSave: @escaping (Alarm) -> Void) {
        self.existingAlarm = alarm
        self.onSave = onSave
        _time = State(initialValue: alarm?.time ?? Date())
        _label = State(initialValue: alarm?.label ?? "Alarm")
        _repeatDays = State(initialValue: alarm?.repeatDays ?? [])
        _exercise = State(initialValue: alarm?.exercise ?? .pushups)
        _repTarget = State(initialValue: alarm?.repTarget ?? ExerciseType.pushups.defaultTarget)
        _snoozeEnabled = State(initialValue: alarm?.snoozeEnabled ?? true)
    }

    var body: some View {
        NavigationStack {
            ZStack {
                Theme.background.ignoresSafeArea()
                ScrollView {
                    VStack(spacing: 24) {
                        DatePicker("", selection: $time, displayedComponents: .hourAndMinute)
                            .datePickerStyle(.wheel)
                            .labelsHidden()
                            .colorScheme(.dark)
                            .cardStyle()

                        VStack(alignment: .leading, spacing: 12) {
                            sectionTitle("Label")
                            TextField("Alarm", text: $label)
                                .textFieldStyle(.plain)
                                .padding()
                                .background(Theme.surfaceElevated)
                                .clipShape(RoundedRectangle(cornerRadius: Theme.controlCorner))
                        }

                        VStack(alignment: .leading, spacing: 12) {
                            sectionTitle("Repeat")
                            WeekdayPicker(selection: $repeatDays)
                        }

                        VStack(alignment: .leading, spacing: 12) {
                            sectionTitle("Exercise to Stop Alarm")
                            ForEach(ExerciseType.allCases) { type in
                                ExerciseOptionRow(
                                    type: type,
                                    isSelected: exercise == type,
                                    repTarget: $repTarget
                                ) {
                                    exercise = type
                                    repTarget = type.defaultTarget
                                }
                            }
                        }

                        Toggle(isOn: $snoozeEnabled) {
                            Text("Allow Snooze")
                                .foregroundStyle(Theme.textPrimary)
                        }
                        .tint(Color(red: 1.0, green: 0.55, blue: 0.26))
                        .padding()
                        .background(Theme.surfaceElevated)
                        .clipShape(RoundedRectangle(cornerRadius: Theme.controlCorner))
                    }
                    .padding()
                }
            }
            .navigationTitle(existingAlarm == nil ? "New Alarm" : "Edit Alarm")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { save() }
                        .fontWeight(.semibold)
                }
            }
        }
    }

    private func sectionTitle(_ text: String) -> some View {
        Text(text.uppercased())
            .font(.caption.bold())
            .foregroundStyle(Theme.textSecondary)
    }

    private func save() {
        let components = Calendar.current.dateComponents([.hour, .minute], from: time)
        var alarm = existingAlarm ?? Alarm(hour: 0, minute: 0)
        alarm.hour = components.hour ?? 0
        alarm.minute = components.minute ?? 0
        alarm.label = label.isEmpty ? "Alarm" : label
        alarm.repeatDays = repeatDays
        alarm.exercise = exercise
        alarm.repTarget = repTarget
        alarm.snoozeEnabled = snoozeEnabled
        onSave(alarm)
        dismiss()
    }
}

private struct WeekdayPicker: View {
    @Binding var selection: Weekday

    var body: some View {
        HStack(spacing: 8) {
            ForEach(Array(Weekday.ordered.enumerated()), id: \.offset) { _, item in
                let (day, letter) = item
                let isSelected = selection.contains(day)
                Button {
                    if isSelected {
                        selection.remove(day)
                    } else {
                        selection.insert(day)
                    }
                } label: {
                    Text(letter)
                        .font(.subheadline.bold())
                        .frame(width: 36, height: 36)
                        .background(isSelected ? AnyShapeStyle(Theme.energy) : AnyShapeStyle(Theme.surfaceElevated))
                        .foregroundStyle(.white)
                        .clipShape(Circle())
                }
            }
        }
    }
}

private struct ExerciseOptionRow: View {
    let type: ExerciseType
    let isSelected: Bool
    @Binding var repTarget: Int
    let onSelect: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            Button(action: onSelect) {
                HStack {
                    Image(systemName: type.iconSystemName)
                        .foregroundStyle(isSelected ? Color(red: 1.0, green: 0.55, blue: 0.26) : Theme.textSecondary)
                    Text(type.displayName)
                        .foregroundStyle(Theme.textPrimary)
                    Spacer()
                    if isSelected {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundStyle(Theme.success)
                    }
                }
            }

            if isSelected {
                Stepper("Target: \(repTarget) reps", value: $repTarget, in: type.targetRange)
                    .foregroundStyle(Theme.textPrimary)
                    .font(.subheadline)
            }
        }
        .padding()
        .background(Theme.surfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: Theme.controlCorner))
    }
}
