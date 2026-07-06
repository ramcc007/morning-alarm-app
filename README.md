# MorningAlarm

An Android alarm clock that only stops once your camera confirms you've done
your exercise — starting with push-ups.

- **`android-app/`** — the Kotlin + Jetpack Compose app (native, Android 8.0+ / API 26+). See `android-app/README.md` for how to open it in Android Studio, run it on-device for free, and configure the Play Billing subscription. Runs on Windows, macOS, or Linux — no Mac required.
- **`website/`** — the static marketing site (hero, how it works, features, pricing, FAQ, waitlist). See `website/README.md` to preview or deploy it. Auto-deploys to GitHub Pages via `.github/workflows/deploy-pages.yml`.

## Product summary

- Free 3-day trial, then **₹50/month** auto-renewing subscription via Google Play Billing (base plan + free-trial offer configured in Play Console, as required for unlocking app functionality).
- Exercise validation runs entirely on-device via Google's ML Kit Pose Detection — no video ever leaves the phone.
- Ships with push-ups; the detector interface (`RepDetector`) is designed so new exercises (squats, jumping jacks, ...) can be added without touching the rest of the app.
- Alarm rings via a full-screen intent that launches straight over the lock screen — the same mechanism used by Alarmy and other camera/puzzle-based alarm apps.
