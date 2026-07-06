import SwiftUI
import UIKit

struct AlarmRingingView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var ringingCoordinator: RingingCoordinator
    @StateObject private var viewModel: AlarmRingingViewModel
    @State private var showSnoozeConfirmation = false

    init(alarm: Alarm) {
        _viewModel = StateObject(wrappedValue: AlarmRingingViewModel(alarm: alarm))
    }

    var body: some View {
        ZStack {
            Theme.background.ignoresSafeArea()

            if viewModel.cameraManager.authorizationDenied {
                cameraDeniedState
            } else {
                CameraPreviewView(session: viewModel.cameraManager.session)
                    .ignoresSafeArea()
                    .overlay(alignment: .top) { header }
                    .overlay(alignment: .bottom) { footer }
                    .overlay { if viewModel.isComplete { completionOverlay } }
            }
        }
        .statusBarHidden()
        .task { viewModel.start() }
        .onDisappear { viewModel.stop() }
        .onChange(of: viewModel.isComplete) { _, isComplete in
            guard isComplete else { return }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.6) {
                ringingCoordinator.clear()
                dismiss()
            }
        }
        .confirmationDialog(
            "Snooze for \(viewModel.alarm.snoozeMinutes) minutes?",
            isPresented: $showSnoozeConfirmation,
            titleVisibility: .visible
        ) {
            Button("Snooze", role: .none) { snooze() }
            Button("Cancel", role: .cancel) {}
        }
    }

    private var header: some View {
        VStack(spacing: 6) {
            Text(viewModel.alarm.label)
                .font(.title2.bold())
                .foregroundStyle(.white)
            Text("\(viewModel.repCount) / \(viewModel.targetReps) \(viewModel.alarm.exercise.displayName.lowercased())")
                .font(.headline)
                .foregroundStyle(.white.opacity(0.85))

            ProgressView(value: viewModel.progress)
                .tint(Theme.success)
                .frame(width: 200)

            if let guidance = viewModel.guidance {
                Text(guidance)
                    .font(.footnote)
                    .foregroundStyle(.yellow)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
                    .padding(.top, 4)
            }
        }
        .padding(.top, 60)
        .padding(.horizontal)
        .frame(maxWidth: .infinity)
        .background(LinearGradient(colors: [.black.opacity(0.55), .clear], startPoint: .top, endPoint: .bottom))
    }

    private var footer: some View {
        VStack(spacing: 12) {
            if viewModel.alarm.snoozeEnabled {
                Button {
                    showSnoozeConfirmation = true
                } label: {
                    Text("Snooze \(viewModel.alarm.snoozeMinutes) min")
                        .font(.subheadline.bold())
                        .foregroundStyle(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(.white.opacity(0.15))
                        .clipShape(Capsule())
                }
            }
            Text("The alarm keeps ringing until you finish your reps.")
                .font(.caption)
                .foregroundStyle(.white.opacity(0.6))
        }
        .padding(.bottom, 40)
        .frame(maxWidth: .infinity)
        .background(LinearGradient(colors: [.clear, .black.opacity(0.6)], startPoint: .top, endPoint: .bottom))
    }

    private var completionOverlay: some View {
        VStack(spacing: 16) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 80))
                .foregroundStyle(Theme.success)
            Text("Nice work! Alarm dismissed.")
                .font(.title3.bold())
                .foregroundStyle(.white)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(.black.opacity(0.75))
        .transition(.opacity)
    }

    private var cameraDeniedState: some View {
        VStack(spacing: 16) {
            Image(systemName: "camera.fill.badge.ellipsis")
                .font(.system(size: 48))
                .foregroundStyle(.white.opacity(0.8))
            Text("Camera access is required to validate your exercise and stop the alarm.")
                .multilineTextAlignment(.center)
                .foregroundStyle(.white)
                .padding(.horizontal, 32)
            Button("Open Settings") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
            .buttonStyle(PrimaryButtonStyle())
            .padding(.horizontal, 48)

            if viewModel.alarm.snoozeEnabled {
                Button("Snooze \(viewModel.alarm.snoozeMinutes) min") {
                    showSnoozeConfirmation = true
                }
                .foregroundStyle(.white.opacity(0.7))
            }
        }
    }

    private func snooze() {
        viewModel.stop()
        NotificationManager.shared.scheduleSnooze(for: viewModel.alarm)
        ringingCoordinator.clear()
        dismiss()
    }
}
