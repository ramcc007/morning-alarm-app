# MorningAlarm (iOS)

An alarm clock that won't stop ringing until you prove — on camera — that
you've done your exercise. Ships with push-ups; the detector is pluggable so
squats, jumping jacks, etc. can be added later.

- **UI**: SwiftUI, dark "energetic sunrise" theme
- **Rep counting**: Apple Vision (`VNDetectHumanBodyPoseRequest`), fully on-device, no ML training or network calls
- **Alarms**: local notifications via `UNUserNotificationCenter`
- **Subscription**: StoreKit 2, 3-day free trial then a ₹50/month auto-renewing subscription
- **Minimum iOS version**: 17.0

## Project layout

```
ios-app/
  project.yml                  # XcodeGen spec — generates the .xcodeproj
  MorningAlarm/
    App/                       # App entry point + AppDelegate (notification routing)
    Models/                    # Alarm, Weekday, ExerciseType
    Services/                  # NotificationManager, AlarmStore, CameraManager,
                                # PushupDetector, AlarmAudioPlayer, SubscriptionManager
    ViewModels/                # AlarmRingingViewModel
    Views/                     # All SwiftUI screens
    Resources/                 # Assets.xcassets, Info.plist (generated), alarm sounds
    Configuration/
      Products.storekit         # Local StoreKit test config (trial + monthly price)
```

## One-time setup (you'll need a Mac)

This repo does not include a pre-built `.xcodeproj` — it's generated from
`project.yml` with [XcodeGen](https://github.com/yonaskolb/XcodeGen), so the
project file never goes stale or produces noisy merge conflicts.

```bash
brew install xcodegen
cd ios-app
xcodegen generate
open MorningAlarm.xcodeproj
```

Then in Xcode:

1. Select the `MorningAlarm` target → **Signing & Capabilities** → pick your
   Apple Developer team (Automatic signing is already configured).
2. Add at least one `.caf` alarm sound to `MorningAlarm/Resources/Sounds/`
   (see the `README.md` in that folder for a one-line `afconvert` command).
   Without this, the in-app alarm loop has nothing to play.
3. Build & run on a **physical device** — the Simulator has no camera, so
   push-up detection cannot be tested there. Body-pose tracking also
   generally needs a real camera feed to be meaningful.
4. For local subscription testing: Product → Scheme → Edit Scheme → Options →
   set **StoreKit Configuration** to `Configuration/Products.storekit`, then
   run. This lets you test the trial + purchase + restore flow without any
   App Store Connect setup or real money.

## App Store Connect setup (before shipping)

1. Create the app record, bundle ID `com.morningalarm.app`.
2. Create an auto-renewing subscription group ("MorningAlarm Plus") with one
   subscription, product ID `com.morningalarm.app.monthly`, priced at **₹50/month**
   (set the India storefront price directly; other territories will get
   Apple's auto-generated equivalent unless you customize them).
3. Add a **3-day free trial introductory offer** to that subscription — this
   is configured entirely in App Store Connect, not in the app's code.
4. Fill in the required subscription metadata (display name, description,
   review screenshot of the paywall) — Apple requires this for any
   subscription submission.
5. Host a real Privacy Policy and Terms of Use (the website in `../website`
   has stub pages for these — replace the placeholder legal text with real
   copy, ideally reviewed by a lawyer, before submitting) and update the URLs
   in `PaywallView.swift` and `SettingsView.swift` to match.

## Important iOS platform constraints (read before you promise features)

- **No background camera access.** iOS never lets any app use the camera
  while backgrounded. The notification sound rings the phone as usual when
  it's locked or the app isn't open; the actual "do 3 push-ups to stop it"
  camera flow only starts once the user opens the app (tapping the
  notification banner routes straight into it, see `AppDelegate.swift`).
  This is the same constraint every camera-based alarm app on the App Store
  works within — there's no way around it on iOS.
- **Local notification sounds cap at 30 seconds** and don't loop natively.
  `AlarmAudioPlayer` takes over looping once the app is foregrounded, using
  an `AVAudioSession` `.playback` category so it plays even with the silent
  switch on, matching standard alarm-app behavior.
- **The subscription is required for unlocking in-app functionality**, so it
  must go through StoreKit/In-App Purchase per App Store Review Guideline
  3.1.1 — do not wire up Stripe or any other billing provider for this.

## Extending to new exercises

1. Add a case to `ExerciseType` (`Models/ExerciseType.swift`).
2. Implement `RepDetector` (see `PushupDetector.swift` as the reference) with
   whatever joint-angle or motion logic fits the movement.
3. Wire the new case into the `switch` in `AlarmRingingViewModel.init`.

## Known limitations / follow-ups worth doing next

- Rep counting uses a 2D joint-angle heuristic (Vision doesn't give real 3D
  poses on a single camera) — it's tuned for a phone propped up ~1-2 meters
  away, at roughly chest height, facing the user in a plank position. Expect
  to tune `downThresholdDegrees` / `upThresholdDegrees` in `PushupDetector`
  once you've tested against real people/rooms.
- There's no server component; entitlement state is purely local via
  StoreKit's `Transaction.currentEntitlements`. Fine for a single-platform
  app; you'd need a backend + receipt validation only if you later want to
  share subscription state across platforms or do server-side analytics.
- No unit/UI tests yet — the state machine in `PushupDetector` and the
  `Weekday`/`Alarm` notification-identifier logic are the highest-value
  places to start.
