# MorningAlarm website

Static marketing site — no build step, no framework, no dependencies.

```
website/
  index.html      # Landing page: hero, animated sunrise scroll, how it works,
                   # features, pricing, FAQ, final CTA
  privacy.html     # Draft privacy policy (needs legal review before launch)
  terms.html       # Draft terms of use (needs legal review before launch)
  css/style.css
  js/main.js       # Mobile nav toggle, scroll-driven sunrise animation,
                    # IntersectionObserver step-reveal
```

Every "Get the APK" / "Download the APK" button links directly to:
`https://github.com/ramcc007/morning-alarm-app/releases/download/debug-latest/morningalarm.apk`

That's a permanent URL — `.github/workflows/build-apk.yml` replaces the
`debug-latest` release's APK on every successful build, so the link never
needs to be updated by hand and never expires (unlike a workflow artifact
link, which requires GitHub login and expires after 30 days).

## Preview locally

```bash
cd website
python3 -m http.server 8000
# open http://localhost:8000
```

## Deploy to Vercel (recommended, free)

1. Go to [vercel.com](https://vercel.com) and sign in with the GitHub account that owns this repo.
2. **Add New → Project**, pick `ramcc007/morning-alarm-app`.
3. On the configure screen:
   - **Root Directory** → click Edit, select `website`.
   - **Framework Preset** → "Other" (no build step needed).
   - Leave Build Command / Output Directory blank.
4. Click **Deploy**. Vercel gives you a `*.vercel.app` URL immediately, and
   redeploys automatically on every push to this branch (Vercel watches the
   whole repo but only rebuilds output from the `website` root you set).
5. Optional: **Project Settings → Domains** to attach a custom domain later.

That's the whole setup — no `vercel.json` needed for a plain static site.

## Alternative: GitHub Pages (already wired up)

A workflow at `.github/workflows/deploy-pages.yml` deploys this folder
automatically on every push that touches `website/`. One-time setup:

1. In the GitHub repo, go to **Settings → Pages**.
2. Under **Build and deployment → Source**, choose **GitHub Actions**.
3. Push to this branch (or re-run the workflow from the **Actions** tab) —
   the site publishes to `https://<your-github-username>.github.io/<repo-name>/`.

You can run this *and* Vercel simultaneously if you want — they don't conflict.

## Before going live

- [ ] Once the app is on Google Play, swap every `data-apk-link` button href from the direct APK URL to the Play Store listing (search `data-apk-link` in `index.html`).
- [ ] Have `privacy.html` and `terms.html` reviewed by a lawyer — they're solid drafts covering the app's actual data practices (on-device camera processing, Play Billing subscriptions, no analytics SDKs) but are not a substitute for legal review.
- [ ] Swap the illustrated phone mockup in the hero for a real device screenshot once you have one from Android Studio/a physical device.
- [ ] Update `support@morningalarm.app` and any other placeholder contact/domain references to your real domain and inbox.
- [ ] The APK is currently **debug-signed** (fine for direct-download early access, but replace with a properly release-signed + Play App Signing build before treating this as a real public distribution channel long-term).
