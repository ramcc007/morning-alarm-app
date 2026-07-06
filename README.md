# MorningAlarm

An iOS alarm clock that only stops once your camera confirms you've done
your exercise — starting with push-ups.

- **`ios-app/`** — the SwiftUI app (native, iOS 17+). See `ios-app/README.md`
  for how to generate the Xcode project (via XcodeGen), run it on-device,
  and configure the StoreKit subscription. **Requires a Mac with Xcode** —
  not buildable on this Linux container.
- **`website/`** — the static marketing site (hero, how it works, features,
  pricing, FAQ, waitlist). See `website/README.md` to preview or deploy it.

## Product summary

- Free 3-day trial, then **₹50/month** auto-renewing subscription (StoreKit 2 / Apple In-App Purchase, as required for unlocking app functionality).
- Exercise validation runs entirely on-device via Apple's Vision framework — no video ever leaves the phone.
- Ships with push-ups; the detector interface (`RepDetector`) is designed so new exercises (squats, jumping jacks, ...) can be added without touching the rest of the app.
