# MorningAlarm website

Static marketing site — no build step, no dependencies.

```
website/
  index.html      # Landing page: hero, how it works, features, testimonials, pricing, FAQ, waitlist
  privacy.html    # Draft privacy policy (needs legal review before launch)
  terms.html      # Draft terms of use (needs legal review before launch)
  css/style.css
  js/main.js      # Mobile nav toggle + client-side waitlist form handling
```

## Preview locally

```bash
cd website
python3 -m http.server 8000
# open http://localhost:8000
```

## Deploy (free)

**GitHub Pages (already wired up)**
A workflow at `.github/workflows/deploy-pages.yml` deploys this folder
automatically on every push that touches `website/`. One-time setup:

1. In the GitHub repo, go to **Settings → Pages**.
2. Under **Build and deployment → Source**, choose **GitHub Actions**.
3. Push to this branch (or re-run the workflow from the **Actions** tab) —
   the site publishes to `https://<your-github-username>.github.io/<repo-name>/`.

No further action needed after that first switch; every future push redeploys it.

**Netlify / Vercel** (also free tiers)
Drag-and-drop the `website/` folder into the Netlify dashboard, or connect
the repo and set the site's base directory to `website/` with no build command.

## Before going live

- [ ] Replace the "Coming soon" / waitlist copy with a real Google Play link once the app is approved.
- [ ] Wire the waitlist form (`js/main.js`) to a real email capture backend (Mailchimp, ConvertKit, a serverless function, etc.) — right now it only shows a client-side confirmation message and doesn't store anything.
- [ ] Have `privacy.html` and `terms.html` reviewed by a lawyer — they're solid drafts covering the app's actual data practices (on-device camera processing, Play Billing subscriptions, no analytics SDKs) but are not a substitute for legal review.
- [ ] Swap the illustrated phone mockups in the hero for real device screenshots once you have build screenshots from Android Studio.
- [ ] Update `support@morningalarm.app` and any other placeholder contact/domain references to your real domain and inbox.
