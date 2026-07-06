import SwiftUI

struct PaywallView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var subscriptionManager: SubscriptionManager
    @State private var isPurchasing = false

    private let benefits: [(icon: String, text: String)] = [
        ("infinity", "Unlimited active alarms"),
        ("figure.strengthtraining.traditional", "Every exercise type, present and future"),
        ("bell.badge.fill", "Custom sounds, snooze rules & smart repeats"),
        ("sparkles", "New exercises added at no extra cost")
    ]

    var body: some View {
        NavigationStack {
            ZStack {
                Theme.background.ignoresSafeArea()
                ScrollView {
                    VStack(spacing: 28) {
                        VStack(spacing: 8) {
                            Image(systemName: "alarm.waves.left.and.right.fill")
                                .font(.system(size: 48))
                                .foregroundStyle(Theme.energy)
                            Text("MorningAlarm Plus")
                                .font(.title.bold())
                                .foregroundStyle(.white)
                            Text("Wake up. Earn it. Own your morning.")
                                .font(.subheadline)
                                .foregroundStyle(Theme.textSecondary)
                        }
                        .padding(.top, 24)

                        VStack(alignment: .leading, spacing: 18) {
                            ForEach(benefits, id: \.text) { benefit in
                                HStack(spacing: 14) {
                                    Image(systemName: benefit.icon)
                                        .font(.title3)
                                        .foregroundStyle(Theme.success)
                                        .frame(width: 28)
                                    Text(benefit.text)
                                        .foregroundStyle(.white)
                                    Spacer()
                                }
                            }
                        }
                        .padding(20)
                        .cardStyle()

                        VStack(spacing: 8) {
                            Text(subscriptionManager.trialDescription ?? "3-day free trial, then ₹50.00/month")
                                .font(.headline)
                                .foregroundStyle(.white)
                            Text("Cancel anytime in the App Store. No charge until your trial ends.")
                                .font(.caption)
                                .foregroundStyle(Theme.textSecondary)
                                .multilineTextAlignment(.center)
                        }
                        .padding(.horizontal)

                        Button {
                            Task {
                                isPurchasing = true
                                await subscriptionManager.purchaseMonthly()
                                isPurchasing = false
                                if subscriptionManager.isSubscribed { dismiss() }
                            }
                        } label: {
                            if isPurchasing {
                                ProgressView().tint(.white)
                            } else {
                                Text("Start Free Trial")
                            }
                        }
                        .buttonStyle(PrimaryButtonStyle())
                        .disabled(isPurchasing)
                        .padding(.horizontal)

                        Button("Restore Purchases") {
                            Task { await subscriptionManager.restorePurchases() }
                        }
                        .font(.footnote)
                        .foregroundStyle(Theme.textSecondary)

                        if let error = subscriptionManager.lastErrorMessage {
                            Text(error)
                                .font(.footnote)
                                .foregroundStyle(.red)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal)
                        }

                        HStack(spacing: 16) {
                            Link("Terms of Use", destination: URL(string: "https://morningalarm.app/terms")!)
                            Link("Privacy Policy", destination: URL(string: "https://morningalarm.app/privacy")!)
                        }
                        .font(.caption2)
                        .foregroundStyle(Theme.textSecondary)
                        .padding(.bottom, 24)
                    }
                    .padding()
                }
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Not Now") { dismiss() }
                        .foregroundStyle(Theme.textSecondary)
                }
            }
        }
    }
}
