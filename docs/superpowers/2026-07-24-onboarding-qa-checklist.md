# Onboarding manual QA checklist

Branch: `feat/onboarding`. Check off as you go; add notes inline under any failing item.

## Setup

- [ ] Build and install: `./gradlew :app:installDebug` (app id `com.hedvig.dev.app`, staging backend)
- [ ] Unleash: `disable_onboarding` toggle created, currently OFF
- [ ] Member A ("maximal"): missing co-insured, pet missing ID, no payin method, referral code, cross-sell eligible (expect 9 progress segments)
- [ ] Member B ("complete"): payment connected, nothing missing (expect 4 segments: welcome, consent, phone, theme)
- [ ] Firebase DebugView armed: `adb shell setprop debug.firebase.analytics.app com.hedvig.dev.app`

Reset between runs: Profile > About app > **Reset onboarding** (non-production row), then background + foreground the app. `adb shell pm clear com.hedvig.dev.app` only when you also want to wipe the session cache and consent.

## A. Gate and show-once

- [ ] A1. Fresh login, land on Home: onboarding appears (welcome, no back arrow, progress at segment 1)
- [ ] A2. Segment count matches the member's applicable steps (compare A vs B members)
- [ ] A3. X on any step: back on Home; kill + relaunch: never returns
- [ ] A4. Complete via "Continue to app": same, never returns
- [ ] A5. System back on welcome: predictive-back preview works natively, flow dismisses, AND is marked seen (background + foreground: does not come back)
- [ ] A6. Kill switch: `disable_onboarding` ON + reset onboarding + background/foreground: nothing appears; OFF again + background/foreground: appears
- [ ] A7. Member switch: logout, login as unseen member B: appears with B's data everywhere (phone, incentive, contracts; no member A leakage)
- [ ] A8. Offline first reach of Home: no flow, no crash; back online, kill + relaunch: appears (failure never marks seen)
- [ ] A9. Deep link (`adb shell am start -a android.intent.action.VIEW -d "<staging deep link>"`) with unseen member: deep-linked screen uninterrupted; flow appears once on an idle Home
- [ ] A10. Demo mode: no onboarding

## B. Navigation mechanics

- [ ] B1. Back arrow pops exactly one step; system back matches on every step, predictive preview included
- [ ] B2. Process death mid-flow (background, `adb shell am kill com.hedvig.dev.app`, reopen from recents): step restores, loading, refetch, progress bar returns
- [ ] B3. After a process-death restore, system back on welcome STILL marks seen (the dismissal observer re-arms on resume)
- [ ] B4. Rotate every screen

## C. Per step

### Consent
- [ ] Underlined "Privacy policy" + northeast arrow opens the language-correct URL
- [ ] Allow and Deny are two identical grey pills, Allow on top
- [ ] Allow advances; Settings > Product analytics shows "On"
- [ ] Deny advances; Settings shows "Off"
- [ ] Decide nothing (dismiss flow first): Settings shows "Not set"
- [ ] DebugView: pre-decision events buffered, arrive with `buffered_at_epoch_ms` on Allow
- [ ] DebugView: Deny stops everything including `session_start` (SDK collection off); Datadog still reports

### Phone
- [ ] Field bottom-anchored above Save; no cursor lag while typing
- [ ] Pre-filled with current number
- [ ] Save advances; profile contact info shows the new number
- [ ] Save offline: inline error, typed digits preserved, stays on step
- [ ] Blank number disables Save
- [ ] "Do this later" (grey pill) advances without any mutation
- [ ] Back to the step after a successful save: Save enabled again

### Theme
- [ ] Design-system radio option cards (title + subtitle)
- [ ] Selection applies instantly app-wide, survives relaunch, agrees with the Settings screen row

### Co-insured / Pet ID
- [ ] Centered grey cards with contract pillow images, dark pill "Add"
- [ ] Add opens the existing flow; completing it returns to the step with a checkmark on that card (row does not vanish)
- [ ] Aborting the external flow returns with the row unchanged
- [ ] Fine-print hint above the buttons

### Invite a friend
- [ ] Card with Hampus / Li / Elin, green dots, real "-10 SEK"-style amounts; no referral code shown
- [ ] "Invite a friend" (grey, ABOVE Continue) opens the Forever screen standalone, no bottom nav
- [ ] System back from Forever returns to the invite step with the flow intact

### Connect payment
- [ ] Fine-print caption; single "Connect payment" pill (no do-later; only X)
- [ ] Trustly completion (staging test bank): connected variant (text + Continue, no checkmark icon)
- [ ] Aborted Trustly that left a PENDING method showing "connected" is CORRECT behavior (deliberate)

### Bundle discount
- [ ] Cross-sell pillow images load (fallback painter offline)
- [ ] Grey "See price" pills open the storeUrl in the browser
- [ ] "Continue to app" ends the flow

## D. Regression edges

- [ ] Settings consent row round-trips On / Off / Not set; DebugView agrees
- [ ] Standalone flows unaffected: edit co-insured, chip-id, Trustly, Forever tab, profile settings
- [ ] TalkBack: back/close announce "Go back" / "Close" with 40dp+ targets
- [ ] Known: repo-wide `ktlintCheck` fails on `feature-payments` (pre-existing on develop, not this branch)

## Notes / found issues

-
