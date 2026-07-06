import SwiftUI

struct AlarmListView: View {
    @EnvironmentObject private var alarmStore: AlarmStore
    @EnvironmentObject private var subscriptionManager: SubscriptionManager
    @State private var isPresentingNewAlarm = false
    @State private var editingAlarm: Alarm?
    @State private var showPaywall = false

    var body: some View {
        NavigationStack {
            ZStack {
                Theme.background.ignoresSafeArea()

                if alarmStore.alarms.isEmpty {
                    emptyState
                } else {
                    List {
                        ForEach(alarmStore.alarms) { alarm in
                            AlarmRow(alarm: alarm, onToggle: { enabled in
                                toggleAlarm(alarm, enabled: enabled)
                            })
                            .listRowBackground(Theme.background)
                            .listRowSeparatorTint(Color.white.opacity(0.08))
                            .contentShape(Rectangle())
                            .onTapGesture { editingAlarm = alarm }
                        }
                        .onDelete { alarmStore.delete(at: $0) }
                    }
                    .listStyle(.plain)
                    .scrollContentBackground(.hidden)
                }
            }
            .navigationTitle("Alarms")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        isPresentingNewAlarm = true
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.title2)
                            .foregroundStyle(Theme.energy)
                    }
                }
            }
            .sheet(isPresented: $isPresentingNewAlarm) {
                AddEditAlarmView(alarm: nil) { newAlarm in
                    alarmStore.add(newAlarm)
                }
            }
            .sheet(item: $editingAlarm) { alarm in
                AddEditAlarmView(alarm: alarm) { updated in
                    alarmStore.update(updated)
                }
            }
            .sheet(isPresented: $showPaywall) {
                PaywallView()
            }
        }
    }

    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: "figure.strengthtraining.traditional")
                .font(.system(size: 56))
                .foregroundStyle(Theme.energy)
            Text("No alarms yet")
                .font(.title3.bold())
                .foregroundStyle(Theme.textPrimary)
            Text("Add an alarm that only stops once you've earned it.")
                .font(.subheadline)
                .foregroundStyle(Theme.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            Button {
                isPresentingNewAlarm = true
            } label: {
                Text("Create Your First Alarm")
            }
            .buttonStyle(PrimaryButtonStyle())
            .padding(.horizontal, 48)
            .padding(.top, 8)
        }
    }

    private func toggleAlarm(_ alarm: Alarm, enabled: Bool) {
        if enabled && !subscriptionManager.isSubscribed && alarmStore.alarms.filter(\.isEnabled).count >= 1 {
            // Free (post-trial, non-subscribed) users can keep one active alarm;
            // enabling more nudges them to the paywall.
            showPaywall = true
            return
        }
        alarmStore.setEnabled(alarm, enabled: enabled)
    }
}

private struct AlarmRow: View {
    let alarm: Alarm
    let onToggle: (Bool) -> Void

    var body: some View {
        HStack(spacing: 16) {
            VStack(alignment: .leading, spacing: 6) {
                Text(alarm.timeString)
                    .font(.system(size: 30, weight: .semibold, design: .rounded))
                    .foregroundStyle(alarm.isEnabled ? Theme.textPrimary : Theme.textSecondary)

                HStack(spacing: 6) {
                    Image(systemName: alarm.exercise.iconSystemName)
                        .font(.caption)
                    Text("\(alarm.repTarget) \(alarm.exercise.displayName.lowercased()) · \(alarm.repeatSummary)")
                        .font(.subheadline)
                }
                .foregroundStyle(Theme.textSecondary)
            }

            Spacer()

            Toggle("", isOn: Binding(
                get: { alarm.isEnabled },
                set: { onToggle($0) }
            ))
            .labelsHidden()
            .tint(Color(red: 1.0, green: 0.55, blue: 0.26))
        }
        .padding(.vertical, 8)
    }
}
