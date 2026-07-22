# Onboarding feature: design spec

Date: 2026-07-21
Figma: https://www.figma.com/design/SogcacjzOxkCC46XcZP8lQ/App-P2-2026?node-id=597-1191&m=dev ("Final Design App Onboarding")

## Overview

A post-login onboarding wizard that walks a new member through setting up their account: a
multi-step, full-screen flow with a segmented progress bar, a back arrow, and a close (X) at
the top. The member's step path is computed on-device from a single eagerly-fetched backend
query, so the progress bar count is known before step 1 renders and no mid-flow network
fetch can strand the member between steps.

Shown once per member: on the first login for a member on this install, and never again once
they complete or dismiss it.

## Goals

- Show the wizard exactly once per member, remembered client-side.
- Fetch all flow data in one query before showing the flow; steps never block on network to
  render.
- Skip steps that do not apply to this member (already-connected payment, no missing
  co-insured, and so on); always show the phone number step for confirmation.
- Keep all step UI inside the onboarding chrome. Performing a real action (adding a
  co-insured person, entering a pet ID, connecting Trustly) navigates out to the existing
  feature flow; when it finishes, navigation pops back to the onboarding step, which stays in
  the back stack.
- Introduce the product analytics consent mechanism (new to the app) as part of this work.

## Non-goals

- No backend/schema changes. The client composes the path from existing Octopus fields.
- No iOS work (iOS hosts its own navigation; this is Android-only).
- Lokalise strings: all new copy ships hardcoded in English with `// TODO: Add ... to
  Lokalise` comments, per project convention.

## Module layout

New module: `app/feature/feature-onboarding/`.

```kotlin
hedvig {
  apollo("octopus")
  compose()
  serialization()
  navKeys()
  viewModels()
}
```

Owns: the `OnboardingQuery`, `OnboardingData` mapping, the pure path builder, the
`OnboardingSessionStore`, the `OnboardingSeenStore`, all step screens, the gate effect
composable, and `fun EntryProviderScope<HedvigNavKey>.onboardingEntries(...)`.

No `feature-onboarding-navigation` module: only `:app` navigates into onboarding, and `:app`
already depends on the feature module. The root `OnboardingKey` is `public` in the feature
module; per-step keys are `internal`.

Dependencies (data/infra plus public nav-key modules only, no feature-to-feature violation):

- `apollo-octopus-public`, `molecule-public`, `navigation-compose`, `design-system-hedvig`,
  `core-common-public`, `core-datastore-public`
- `data-settings-datastore-public` (theme + analytics consent)
- `feature-edit-coinsured-navigation` (`EditCoInsuredTriageKey`, `CoInsuredAddInfoKey`)
- `feature-connect-payment-trustly-navigation` (`TrustlyKey`)
- chip-id public key module (`ChipIdKey`)
- auth/member-id access for the per-member seen flag

## Data: eager fetch and path construction

One internal `OnboardingQuery` (new `.graphql` file in the module) fetches everything the
flow needs in a single round trip:

```
currentMember {
  email
  phoneNumber
  activeContracts {
    id
    exposureDisplayNameShort
    coInsured { firstName lastName hasMissingInfo ... }
    isMissingPetId
    currentAgreement { productVariant { typeOfContract displayName } }
  }
  referralInformation { code monthlyDiscountPerReferral }
  paymentMethods { payinMethods { ... } }
  crossSellV2(input: ...) { recommendedCrossSell { ... } otherCrossSells { ... } }
}
```

Mapped to a project-owned `OnboardingData`; `octopus.*` types stay confined to the internal
repository impl, per the data-layer rule.

`buildOnboardingPath(data: OnboardingData): List<OnboardingStep>` is a pure function and the
primary unit-test target. Step inclusion rules:

| Step | Included when |
| --- | --- |
| Welcome | always |
| Analytics consent | always (it is the flow's one-time ask) |
| Phone number | always, pre-filled with the current number for confirmation |
| Theme | always |
| Add co-insured | any active contract has a co-insured with `hasMissingInfo == true` |
| Pet ID numbers | any active contract has `isMissingPetId == true` |
| Invite a friend | `referralInformation.code` exists |
| Connect payment | `payinMethods` is empty |
| Bundle discount | eligible cross-sells returned; per the Figma annotation, skipped when the member has accident-only insurance (exact rule read off the `crossSellV2` response at implementation) |

The path length drives the segmented progress bar.

## Show-once mechanism

`OnboardingSeenStore`, DataStore-backed (same pattern as `DeviceIdDataStore` /
`SettingsDataStoreImpl`), with a per-member key:

```
booleanPreferencesKey("com.hedvig.android.feature.onboarding.seen.<memberId>")
```

Set to true when the member completes the flow ("Continue to app") or closes it with X.
Never shown again for that member on this install. A different member logging in on the same
device gets their own flag.

## Gate: when the flow appears

An effect composable exposed by `feature-onboarding` and placed in `HedvigApp`, at the same
seam as `TryShowAppStoreReviewDialogEffect` and `CrossSellSheet`:

1. Wait for `AuthStatus.LoggedIn` and session readiness.
2. Read the member id; check `OnboardingSeenStore`. If seen: do nothing.
3. If unseen: run `OnboardingQuery`. On failure: do nothing, leave the flag unset, retry on
   next app start. Login/splash is never held on this fetch.
4. On success, compute the path. If non-empty: store `OnboardingData` + path in the
   `OnboardingSessionStore` and push `OnboardingKey`, but only while the current destination
   is the Home tab root and no deep link is pending, so a deep-linked flow is never
   interrupted.

## Navigation model

Per-step nav keys over a shared session store:

- `OnboardingKey` (public): the root, renders the Welcome step.
- `OnboardingStepKey(stepId: OnboardingStepId)` (internal): one entry per subsequent step.
  `OnboardingStepId` is a serializable enum. Keys are tiny; `navKeys()` registration gives
  process-death survival for free (covered by `ExhaustiveBackStackSerializationTest`).
- `OnboardingSessionStore` is `@SingleIn(ActivityRetainedScope::class)` and holds the fetched
  `OnboardingData` and computed path. Progress = index of the current step id in the path.

Navigation semantics:

- Continue / Get started: `backstack.add(OnboardingStepKey(next))`.
- Top app bar back arrow: `backstack.navigateUp()` (the one sanctioned use).
- X (any step) and the final "Continue to app": mark seen in `OnboardingSeenStore`, then
  remove all onboarding keys from the back stack, landing on Home underneath.
- Navigate-out steps push the existing feature flow (`CoInsuredAddInfoKey`, `ChipIdKey`,
  `TrustlyKey`) on top of the onboarding step. When that flow pops, the member lands back on
  the onboarding step, which refreshes its slice of data so the "done" variant renders.

Process death mid-flow: keys restore the position; the session store is empty, so the visible
step shows a loading state, refetches `OnboardingQuery`, and rebuilds the path. If the
rebuilt path no longer contains the restored step (for example payment got connected
elsewhere), pop to the nearest preceding step still in the path.

## Step screens

All steps render inside a shared `OnboardingStepScaffold`: segmented progress bar (one
segment per path step, filled up to the current index), back arrow (hidden on Welcome), X.
The segmented progress bar is expected to be a new small component; check
`design-system-hedvig` for an existing one before adding it.

1. **Welcome**: Hedvig app icon with a red "1" badge (animated), "Welcome to Hedvig",
   subtitle, "Get started" button. No back arrow.
2. **Analytics consent**: "Help us make the app better" copy, chart icon with checkmark
   animation on Allow, "Privacy policy" external link, Allow / Deny. Writes the tri-state
   consent value (see below); both choices advance.
3. **Phone number**: pre-filled text field with the member's current number, "Save" runs
   `MemberUpdateContactInfoMutation` (email is fetched in `OnboardingQuery` and resubmitted
   unchanged, since the mutation requires both fields), "Do this later" advances without
   saving. Inline error + retry on mutation failure.
4. **Choose theme**: System / Light / Dark radio list backed by
   `SettingsDataStore.setTheme(...)`; selection applies immediately (live theme change),
   "Continue" advances. Hint text: "You can change these settings later".
5. **Add co-insured**: one row per contract with missing co-insured info, "Add" navigates out
   to the edit-co-insured flow for that contract. On return the step refreshes and shows the
   added names with checkmarks (Figma's done variant). Continue / Do this later.
6. **Pet ID numbers**: one row per pet contract with `isMissingPetId`, "Add" navigates out to
   the chip-id flow (`ChipIdKey(contractId)`). Done variant with checkmarks on return.
   Continue / Do this later.
7. **Invite a friend**: referral incentive UI built from `referralInformation`
   (`monthlyDiscountPerReferral`, campaign code). "Invite a friend" opens the system share
   sheet with the code (same share content as the Forever screen). "Continue" advances.
8. **Connect payment**: bank-to-Hedvig animation, "Connect payment" navigates out to the
   Trustly flow (`TrustlyKey`). On return, refetch payment status; if connected, show the
   connected variant (checkmark, "You can switch accounts later in settings") with
   "Continue".
9. **Bundle discount**: cross-sell rows (title, savings text) from `crossSellV2`, each "See
   price" opens the external `storeUrl`. "Continue to app" completes the flow (marks seen,
   pops everything).

## Analytics consent mechanism (new, cross-cutting)

- **Datadog is untouched.** It carries performance and bug analytics, not marketing data;
  its consent is hardcoded as granted, forever. Crashlytics is likewise untouched.
- **Only Firebase Analytics is gated.** Consent is tri-state:
  `NotDecided` (default), `Granted`, `Denied`, stored as a string-backed enum key in
  `SettingsDataStore` (`data-settings-datastore-public`).
- **NotDecided (buffer-and-flush)**: the tracker fan-out queues Firebase-bound events
  instead of forwarding them. On `Granted`, the buffer is replayed into `FirebaseAnalytics`
  (original event timestamps attached as an event parameter, since replayed events carry
  send-time timestamps) and live forwarding begins. On `Denied`, the buffer is dropped and
  nothing is forwarded. Whether the buffer is in-memory or persisted is decided at
  implementation; in-memory is the starting assumption (worst case, pre-decision events from
  earlier process lifetimes are lost, which is acceptable).
  Note: Firebase's own `setAnalyticsCollectionEnabled(false)` drops rather than queues, so
  the buffer must live in our own fan-out layer, not the SDK.
- **Settings toggle**: a new row in the profile Settings screen (`feature-profile`,
  `SettingsPresenter`) reading/writing the same value, fulfilling the design's "can be
  turned off any time in settings".
- The onboarding step writes `Granted` on Allow and `Denied` on Deny.
  On DENIED, the wrapper also forces `setAnalyticsCollectionEnabled(false)` on the Firebase SDK
  (automatic events included), re-enabled on GRANTED subject to the demo-mode gate; decided
  post-review.

## Error handling

- Gate fetch failure: silently skip showing onboarding; flag stays unset; retried next app
  start.
- Refetch failure after process-death restore: error state with retry inside the scaffold;
  X still dismisses (and marks seen).
- Step mutation failure (phone save): inline error, retry available, "Do this later" always
  works.
- Returning from a navigate-out flow: refresh that step's data; on refresh failure keep the
  previously shown state (the member can still Continue).

## Testing

- `buildOnboardingPath`: one unit test per inclusion/skip rule, plus ordering.
- `OnboardingSeenStore`: per-member key behavior with a fake DataStore.
- Gate use case: shows when unseen + fetch succeeds + path non-empty; stays silent on seen /
  fetch failure / empty path / pending deep link.
- Presenter tests (Turbine + Molecule) per step, covering success, failure, and skip events.
- Consent fan-out: NotDecided buffers, Granted flushes then forwards, Denied drops.
- Key serialization: automatic via `ExhaustiveBackStackSerializationTest` once `navKeys()` is
  on.

## Risks / open items (resolved at implementation)

- Exact Firebase suppression point in the tracking fan-out (which class owns forwarding).
- Whether `design-system-hedvig` already has a segmented progress component.
- The precise `crossSellV2` field expressing "accident-only, skip bundle step".
- The chip-id public key module's exact name/coordinates.
- Trustly flow pop-back behavior on success (verify it pops itself back to the caller).
