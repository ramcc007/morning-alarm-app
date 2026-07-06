import SwiftUI

struct RootView: View {
    @EnvironmentObject private var alarmStore: AlarmStore
    @EnvironmentObject private var subscriptionManager: SubscriptionManager
    @EnvironmentObject private var ringingCoordinator: RingingCoordinator
    @State private var showPaywall = false

    var body: some View {
        TabView {
            AlarmListView()
                .tabItem { Label("Alarms", systemImage: "alarm.fill") }

            SettingsView()
                .tabItem { Label("Settings", systemImage: "gearshape.fill") }
        }
        .tint(Color(red: 1.0, green: 0.55, blue: 0.26))
        .task {
            _ = await NotificationManager.shared.requestAuthorization()
            await subscriptionManager.refreshEntitlements()
        }
        .fullScreenCover(item: ringingAlarmBinding) { alarm in
            AlarmRingingView(alarm: alarm)
        }
        .sheet(isPresented: $showPaywall) {
            PaywallView()
        }
        .onChange(of: subscriptionManager.isSubscribed) { _, isSubscribed in
            // Nothing to do here directly; AlarmListView reacts to trial/subscription
            // state itself when the user tries to enable an alarm.
        }
    }

    /// Adapts the coordinator's `UUID?` into an `Identifiable` binding for `fullScreenCover(item:)`.
    private var ringingAlarmBinding: Binding<Alarm?> {
        Binding<Alarm?>(
            get: {
                guard let id = ringingCoordinator.activeAlarmId else { return nil }
                return alarmStore.alarm(withId: id)
            },
            set: { newValue in
                if newValue == nil { ringingCoordinator.clear() }
            }
        )
    }
}
