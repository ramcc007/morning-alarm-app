import SwiftUI

@main
struct MorningAlarmApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @StateObject private var alarmStore = AlarmStore()
    @StateObject private var subscriptionManager = SubscriptionManager.shared
    @StateObject private var ringingCoordinator = RingingCoordinator.shared
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false

    var body: some Scene {
        WindowGroup {
            Group {
                if hasCompletedOnboarding {
                    RootView()
                } else {
                    OnboardingView(isPresented: Binding(
                        get: { !hasCompletedOnboarding },
                        set: { hasCompletedOnboarding = !$0 }
                    ))
                }
            }
            .environmentObject(alarmStore)
            .environmentObject(subscriptionManager)
            .environmentObject(ringingCoordinator)
            .preferredColorScheme(.dark)
        }
    }
}
