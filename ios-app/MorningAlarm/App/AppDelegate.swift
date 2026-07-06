import UIKit
import UserNotifications

/// Bridges `UNUserNotificationCenter` callbacks into `RingingCoordinator`.
///
/// Important iOS constraint: apps cannot run the camera in the background.
/// The alarm's notification sound plays via the system while the app is
/// backgrounded/killed; the actual push-up validation only starts once the
/// user opens the app (either by tapping the notification banner or
/// launching the app manually after hearing it). This class makes sure both
/// paths land on the same `AlarmRingingView`.
final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        NotificationManager.shared.registerCategories()
        return true
    }

    /// Notification arrives while app is in the foreground: show it AND immediately
    /// route to the ringing screen so the user can't just swipe it away.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        handle(notification.request)
        completionHandler([.banner, .sound, .list])
    }

    /// User tapped the notification (app was backgrounded or not running).
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        handle(response.notification.request)
        completionHandler()
    }

    private func handle(_ request: UNNotificationRequest) {
        guard let idString = request.content.userInfo[NotificationManager.alarmIdKey] as? String,
              let alarmId = UUID(uuidString: idString) else { return }
        Task { @MainActor in
            RingingCoordinator.shared.trigger(alarmId: alarmId)
        }
    }
}
