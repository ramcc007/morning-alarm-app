import SwiftUI

struct OnboardingView: View {
    @Binding var isPresented: Bool
    @State private var page = 0
    @State private var showPaywall = false

    private struct Page {
        let icon: String
        let title: String
        let subtitle: String
    }

    private let pages: [Page] = [
        Page(icon: "alarm.waves.left.and.right.fill",
             title: "The alarm you can't sleep through",
             subtitle: "No snooze spiral. No shutting it off half-asleep. It rings until you move."),
        Page(icon: "figure.strengthtraining.traditional",
             title: "Prove it with push-ups",
             subtitle: "Point your front camera at yourself and knock out your set. We count every rep automatically."),
        Page(icon: "sunrise.fill",
             title: "Start your day already winning",
             subtitle: "By the time the alarm stops, you've already done your first workout of the day.")
    ]

    var body: some View {
        ZStack {
            Theme.background.ignoresSafeArea()
            VStack {
                TabView(selection: $page) {
                    ForEach(Array(pages.enumerated()), id: \.offset) { index, item in
                        VStack(spacing: 24) {
                            Spacer()
                            Image(systemName: item.icon)
                                .font(.system(size: 72))
                                .foregroundStyle(Theme.energy)
                            Text(item.title)
                                .font(.title.bold())
                                .foregroundStyle(.white)
                                .multilineTextAlignment(.center)
                            Text(item.subtitle)
                                .font(.body)
                                .foregroundStyle(Theme.textSecondary)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 32)
                            Spacer()
                            Spacer()
                        }
                        .tag(index)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .always))
                .indexViewStyle(.page(backgroundDisplayMode: .always))

                Button {
                    if page < pages.count - 1 {
                        withAnimation { page += 1 }
                    } else {
                        showPaywall = true
                    }
                } label: {
                    Text(page < pages.count - 1 ? "Continue" : "Get Started")
                }
                .buttonStyle(PrimaryButtonStyle())
                .padding(.horizontal, 32)
                .padding(.bottom, 40)
            }
        }
        .sheet(isPresented: $showPaywall, onDismiss: { isPresented = false }) {
            PaywallView()
        }
    }
}
