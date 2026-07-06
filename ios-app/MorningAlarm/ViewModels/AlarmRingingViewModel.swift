import Foundation
import Combine

@MainActor
final class AlarmRingingViewModel: ObservableObject {
    let alarm: Alarm
    let cameraManager = CameraManager()

    @Published private(set) var repCount = 0
    @Published private(set) var isComplete = false
    @Published private(set) var guidance: String?

    private let detector: RepDetector
    private let audioPlayer = AlarmAudioPlayer()
    private var guidanceTimer: Timer?

    init(alarm: Alarm) {
        self.alarm = alarm
        switch alarm.exercise {
        case .pushups:
            self.detector = PushupDetector()
        }

        cameraManager.activeDetector = detector
        detector.onRepCompleted = { [weak self] count in
            Task { @MainActor in
                self?.handleRepUpdate(count)
            }
        }
    }

    var targetReps: Int { alarm.repTarget }
    var progress: Double {
        guard targetReps > 0 else { return 0 }
        return min(1, Double(repCount) / Double(targetReps))
    }

    func start() {
        audioPlayer.start(soundName: alarm.soundName)
        cameraManager.configureAndStart()
        guidanceTimer = Timer.scheduledTimer(withTimeInterval: 0.4, repeats: true) { [weak self] _ in
            Task { @MainActor in self?.guidance = self?.detector.currentGuidance }
        }
    }

    func stop() {
        audioPlayer.stop()
        cameraManager.stop()
        guidanceTimer?.invalidate()
        guidanceTimer = nil
    }

    private func handleRepUpdate(_ count: Int) {
        repCount = count
        if count >= targetReps {
            isComplete = true
            audioPlayer.stop()
            cameraManager.stop()
        }
    }
}
