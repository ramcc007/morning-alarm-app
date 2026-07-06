import SwiftUI

struct SettingsView: View {
    @EnvironmentObject private var subscriptionManager: SubscriptionManager
    @State private var showPaywall = false

    var body: some View {
        NavigationStack {
            ZStack {
                Theme.background.ignoresSafeArea()
                List {
                    Section {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(subscriptionManager.isSubscribed ? "MorningAlarm Plus" : "Free")
                                    .font(.headline)
                                    .foregroundStyle(.white)
                                Text(subscriptionManager.isSubscribed
                                     ? "Unlimited alarms & exercises"
                                     : "1 active alarm. Upgrade for unlimited.")
                                    .font(.caption)
                                    .foregroundStyle(Theme.textSecondary)
                            }
                            Spacer()
                            if !subscriptionManager.isSubscribed {
                                Button("Upgrade") { showPaywall = true }
                                    .font(.caption.bold())
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Theme.energy)
                                    .foregroundStyle(.white)
                                    .clipShape(Capsule())
                            }
                        }
                        .listRowBackground(Theme.surface)
                    }

                    Section("About") {
                        Link("Visit our website", destination: URL(string: "https://morningalarm.app")!)
                        Link("Privacy Policy", destination: URL(string: "https://morningalarm.app/privacy")!)
                        Link("Terms of Use", destination: URL(string: "https://morningalarm.app/terms")!)
                        Link("Contact Support", destination: URL(string: "mailto:support@morningalarm.app")!)
                    }
                    .listRowBackground(Theme.surface)
                    .foregroundStyle(.white)

                    Section {
                        Button("Restore Purchases") {
                            Task { await subscriptionManager.restorePurchases() }
                        }
                    }
                    .listRowBackground(Theme.surface)
                    .foregroundStyle(.white)

                    Section {
                        HStack {
                            Text("Version")
                            Spacer()
                            Text(Bundle.main.appVersionString)
                                .foregroundStyle(Theme.textSecondary)
                        }
                    }
                    .listRowBackground(Theme.surface)
                    .foregroundStyle(.white)
                }
                .scrollContentBackground(.hidden)
            }
            .navigationTitle("Settings")
            .sheet(isPresented: $showPaywall) { PaywallView() }
        }
    }
}

private extension Bundle {
    var appVersionString: String {
        let version = infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
        let build = infoDictionary?["CFBundleVersion"] as? String ?? "1"
        return "\(version) (\(build))"
    }
}
