import Foundation
import AVFoundation

/// Loops the alarm sound at full volume while the ringing screen is on
/// screen, ignoring the silent switch (category `.playback`) the way every
/// alarm-clock app on the App Store does. Only `stop()` (called once the
/// required reps are validated, or on snooze) silences it.
final class AlarmAudioPlayer: ObservableObject {
    private var player: AVAudioPlayer?

    func start(soundName: String) {
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playback, options: [.duckOthers])
            try session.setActive(true)

            guard let url = Bundle.main.url(forResource: soundName, withExtension: "caf")
                ?? Bundle.main.url(forResource: "classic_alarm", withExtension: "caf") else {
                print("AlarmAudioPlayer: no bundled sound found for \(soundName)")
                return
            }
            player = try AVAudioPlayer(contentsOf: url)
            player?.numberOfLoops = -1
            player?.volume = 1.0
            player?.play()
        } catch {
            print("AlarmAudioPlayer: failed to start - \(error)")
        }
    }

    func stop() {
        player?.stop()
        player = nil
        try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
    }
}
