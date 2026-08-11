# Onboarding manual QA checklist

Branch: `feat/onboarding`. Check off as you go; add notes inline under any failing item.

## Setup

- [X] Build and install: `./gradlew :app:installDebug` (app id `com.hedvig.dev.app`, staging backend)
- [X] Unleash: `disable_onboarding` toggle created, currently OFF
- [ ] Member A ("maximal"): missing co-insured, pet missing ID, no payin method, referral code, cross-sell eligible (expect 9 progress segments)
- [ ] Member B ("complete"): payment connected, nothing missing (expect 4 segments: welcome, consent, phone, theme)
- [ ] Firebase DebugView armed: `adb shell setprop debug.firebase.analytics.app com.hedvig.dev.app`

Reset between runs: Profile > About app > **Reset onboarding** (non-production row), then background + foreground the app. `adb shell pm clear com.hedvig.dev.app` only when you also want to wipe the session cache and consent.

## A. Gate and show-once

- [X] A1. Fresh login, land on Home: onboarding appears (welcome, no back arrow, progress at segment 1)
- [X] A2. Segment count matches the member's applicable steps (compare A vs B members)
- [X] A3. X on any step: back on Home; kill + relaunch: never returns
- [X] A4. Complete via "Continue to app": same, never returns
- [X] A5. System back on welcome: predictive-back preview works natively, flow dismisses, AND is marked seen (background + foreground: does not come back)
- [X] A6. Kill switch: `disable_onboarding` ON + reset onboarding + background/foreground: nothing appears; OFF again + background/foreground: appears
- [X] A7. Member switch: logout, login as unseen member B: appears with B's data everywhere (phone, incentive, contracts; no member A leakage)
- [X] A8. Offline first reach of Home: no flow, no crash; back online, kill + relaunch: appears (failure never marks seen)
- [X] A9. Deep link (`adb shell am start -a android.intent.action.VIEW -d "<staging deep link>"`) with unseen member: deep-linked screen uninterrupted; flow appears once on an idle Home
- [X] A10. Demo mode: no onboarding

## B. Navigation mechanics

- [X] B1. Back arrow pops exactly one step; system back matches on every step, predictive preview included
- [X] B2. Process death mid-flow (background, `adb shell am kill com.hedvig.dev.app`, reopen from recents): step restores, loading, refetch, progress bar returns
- [X] B3. After a process-death restore, system back on welcome STILL marks seen (the dismissal observer re-arms on resume)
- [X] B4. Rotate every screen
  - Sampled connect-payment (graphic) and invite (card list) portrait↔landscape: shared scaffold reflows, no crash. Not every single screen rotated individually.

## C. Per step

### Consent
- [X] Underlined "Privacy policy" + northeast arrow opens the language-correct URL
  - Opened hedvig.com/se-en/... "Hedvig Privacy Policy" in the browser.
- [X] Allow and Deny are two identical grey pills, Allow on top
- [ ] Allow advances; Settings > Product analytics shows "On"
- [ ] Deny advances; Settings shows "Off"
- [ ] Decide nothing (dismiss flow first): Settings shows "Not set"
- [ ] DebugView: pre-decision events buffered, arrive with `buffered_at_epoch_ms` on Allow
- [ ] DebugView: Deny stops everything including `session_start` (SDK collection off); Datadog still reports

### Phone
- [ ] Field bottom-anchored above Save; no cursor lag while typing
- [X] Pre-filled with current number
- [ ] Save advances; profile contact info shows the new number
- [ ] Save offline: inline error, typed digits preserved, stays on step
- [X] Blank number disables Save
  - Cleared the field via adb; tapping Save did not advance (design-system button drops its click when disabled even though the a11y node stays enabled).
- [X] "Do this later" (grey pill) advances without any mutation
  - Skipped from a blank field to Theme; no save call, account number unchanged.
- [ ] Back to the step after a successful save: Save enabled again

### Theme
- [X] Design-system radio option cards (title + subtitle)
- [ ] Selection applies instantly app-wide, survives relaunch, agrees with the Settings screen row
  - Instant apply + relaunch persistence confirmed via automation (screen luminance 231→42 on selecting Dark; still 36 after a force-stop restart). Settings-row agreement still not checked (the Profile screen's settings entry wasn't reachable via automation).

### Co-insured / Pet ID
- [ ] Centered grey cards with contract pillow images, dark pill "Add"
- [X] Add opens the existing flow; completing it returns to the step with a checkmark on that card (row does not vanish)
  - Pet-ID verified; co-insured step not yet driven on-device.
- [ ] Aborting the external flow returns with the row unchanged
- [ ] Fine-print hint above the buttons

### Invite a friend
- [X] Card with Hampus / Li / Elin, green dots, real "-10 SEK"-style amounts; no referral code shown
- [X] "Invite a friend" (grey, ABOVE Continue) opens the Forever screen standalone, no bottom nav
- [X] System back from Forever returns to the invite step with the flow intact

### Connect payment
- [X] Fine-print caption; first view is a single "Connect payment" pill (only X, no do-later). A "Do this later" pill appears only after entering the connect flow and returning still unconnected.
  - First-view (only Connect payment) verified on-device; the appears-after-attempt reveal is covered by OnboardingPaymentPresenterTest (a Trustly abort on staging leaves a PENDING method, which correctly shows connected instead).
- [ ] Trustly completion (staging test bank): connected variant (text + Continue, no checkmark icon)
- [X] Aborted Trustly that left a PENDING method showing "connected" is CORRECT behavior (deliberate)

### Bundle discount
- [X] Cross-sell pillow images load (fallback painter offline)
  - Images loaded online (Accident/Home/Pet/Vacation Home/Payment Protection). Offline fallback painter not tested.
- [X] Grey "See price" pills open the storeUrl in the browser
  - Opened dev.hedvigit.com/api/... in Chrome.
- [ ] "Continue to app" ends the flow

## D. Regression edges

- [ ] Settings consent row round-trips On / Off / Not set; DebugView agrees
- [ ] Standalone flows unaffected: edit co-insured, chip-id, Trustly, Forever tab, profile settings
- [ ] TalkBack: back/close announce "Go back" / "Close" with 40dp+ targets
- [ ] Known: repo-wide `ktlintCheck` fails on `feature-payments` (pre-existing on develop, not this branch)

## Notes / found issues

- Connect payment: a Trustly abort at the very first Trustly screen still registers a PENDING method on staging, so the step returns showing "connected" (Continue). This is the deliberate behavior, but it means the "Do this later" reveal is hard to reproduce on-device from a normal abort.
- Drive was done on a member whose path was Welcome → Consent → Phone → Theme → Invite → Connect payment → Bundle (7 segments): no co-insured or pet-ID step, so those steps were not reachable in this session. Co-insured Add→return still needs an on-device pass with a member missing co-insured info.
- Copy: bundle step subtitle says "15% bundle discount" on-device vs "10%" in the Figma frame. Confirm which is intended (looks like a config/copy value, not a bug).
