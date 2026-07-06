# MorningAlarm (Android)

An alarm clock that won't stop ringing until you prove — on camera — that
you've done your exercise. Ships with push-ups; rep counting is pluggable so
squats, jumping jacks, etc. can be added later.

- **UI**: Jetpack Compose (Material 3), dark "energetic sunrise" theme
- **Rep counting**: Google ML Kit Pose Detection, fully on-device, no training or network calls
- **Alarms**: `AlarmManager.setAlarmClock` + a full-screen-intent notification that launches the ringing screen straight over the lock screen
- **Subscription**: Google Play Billing Library, 3-day free trial then a ₹50/month auto-renewing subscription
- **Minimum SDK**: 26 (Android 8.0) · **Target/compile SDK**: 34

## Project layout

```
android-app/
  settings.gradle.kts, build.gradle.kts, gradle.properties   # Gradle project config
  app/
    build.gradle.kts                # Dependencies (CameraX, ML Kit, Billing, Compose, DataStore)
    proguard-rules.pro
    src/main/
      AndroidManifest.xml
      kotlin/com/morningalarm/app/
        MainActivity.kt             # Nav host: onboarding -> alarms/settings tabs
        MorningAlarmApplication.kt  # Holds the app-wide AlarmRepository + BillingManager
        model/                      # Alarm, Weekday, ExerciseType
        data/                       # AlarmRepository (DataStore-backed JSON persistence)
        alarm/                      # AlarmScheduler, AlarmReceiver, AlarmRingingService,
                                     # AlarmRingingActivity, BootRescheduleReceiver
        camera/                     # PoseCameraController (CameraX + ML Kit), RepDetector,
                                     # PushupRepDetector
        billing/                   # BillingManager (Play Billing Library)
        ui/                        # ViewModels + Compose screens
      res/                          # Theme, strings, adaptive icon, alarm sound folder
```

## One-time setup

1. Install [Android Studio](https://developer.android.com/studio) (free, runs on Windows/macOS/Linux — **no Mac required**, unlike iOS).
2. Open the `android-app/` folder in Android Studio.
3. This repo doesn't ship the Gradle wrapper jar (it's a binary file). Android
   Studio will offer to generate/repair it automatically on first open; if it
   doesn't, run `gradle wrapper --gradle-version 8.7` once (needs a local
   Gradle install) to create `gradlew` / `gradlew.bat` / the wrapper jar.
4. Add at least one alarm sound to `app/src/main/res/raw/` — see the note in
   that folder's absence: create e.g. `classic_alarm.ogg` there (lowercase,
   letters/digits/underscores only — Android resource naming rules).
   `AlarmRingingService` won't have anything to loop without it.
5. Build & run on a **physical device** for real testing (a webcam can be
   forwarded to the Android Emulator's virtual camera for a rough test, but
   real front-camera pose tracking is best validated on-device).

## Testing for free (no Play Console account needed yet)

Android lets you build, install, and fully test on your own phone via USB
debugging with just Android Studio — no developer account, no fee. You only
need to pay anything when you're ready to **publish**:

- **Google Play Console registration is a one-time $25 fee** (not recurring, unlike Apple's $99/year).
- Everything else — Android Studio, Kotlin, CameraX, ML Kit, Play Billing library — is free.

## Google Play Console setup (before shipping)

1. Create the app listing, applicationId `com.morningalarm.app`.
2. Go to **Monetize → Products → Subscriptions**, create a subscription with
   product ID `morningalarm_plus_monthly` (must match `SUBSCRIPTION_PRODUCT_ID`
   in `BillingManager.kt`).
3. Add a **base plan** priced at **₹50/month**, then add a **free trial offer**
   (3 days) on that base plan — this is configured entirely in Play Console,
   not in app code, the same way Apple's trial is configured in App Store
   Connect rather than in StoreKit code.
4. Upload a signed release build to an internal testing track and add your
   own Google account as a license tester to test real purchases (including
   the trial) without being charged.
5. Fill in the Data Safety form: camera is used only for on-device pose
   detection, nothing is uploaded (see `website/privacy.html` for the
   equivalent language to reuse).

## Why Android suits this app well

Unlike iOS, Android allows a foreground `Service` to post a
**full-screen-intent notification** that the system is required to honor by
launching an `Activity` immediately — even over the lock screen, even if the
app was killed. That's exactly the mechanism `AlarmRingingService` +
`AlarmRingingActivity` use, and it's the same trick alarm apps like Alarmy
rely on. There's no equivalent guarantee on iOS, where camera access is
blocked entirely until the user manually opens the app.

## Extending to new exercises

1. Add a value to `ExerciseType` (`model/ExerciseType.kt`).
2. Implement `RepDetector` (see `PushupRepDetector.kt` as the reference) with
   whatever joint-angle or motion logic fits the movement, using ML Kit's
   `Pose` landmarks.
3. Wire the new case into the `when` in `AlarmRingingViewModel`'s detector selection.

## Known limitations / follow-ups worth doing next

- Rep counting uses a 2D/near-3D joint-angle heuristic (a single RGB camera
  isn't a depth sensor) — tuned for a phone propped up ~1-2 meters away,
  roughly chest height, facing the user in a plank position. Expect to tune
  `downThresholdDegrees` / `upThresholdDegrees` in `PushupRepDetector` once
  tested against real people/rooms.
- `AlarmManager` has no native weekly-repeat primitive, so each weekday is
  scheduled individually and re-armed 7 days out every time it fires (see
  `AlarmScheduler` / `AlarmReceiver`). This is standard practice for Android
  alarm apps but means a missed reschedule (e.g. the receiver crashing) could
  silently drop that weekday until the app is reopened — worth adding a
  periodic `WorkManager` job that reconciles scheduled alarms against saved
  ones as a safety net.
- No server component; entitlement state is purely local via Play Billing's
  `queryPurchasesAsync`. Fine for a single-platform app; you'd need a backend
  + server-side receipt validation only if you want to detect refunds/chargebacks
  proactively or share subscription state across platforms.
- No unit/UI tests yet — the state machine in `PushupRepDetector` and the
  `AlarmScheduler` weekday-scheduling math are the highest-value places to start.
