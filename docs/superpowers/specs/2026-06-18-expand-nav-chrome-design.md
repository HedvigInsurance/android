# Expand navigation chrome to more non-flow destinations

## Goal

Experiment with showing the global navigation chrome (bottom bar / rail) on more
destinations than the current 9. The chrome should appear on screens that are *not*
part of a self-contained flow (claim flow, termination flow, document-generation flow),
starting with read-only info screens under the Payments and Profile tabs.

This is a code-level experiment: no runtime feature flag, no A/B toggle. We expand the
opt-in set, build, and evaluate in the running app.

## Background: how chrome visibility works today

- The chrome is rendered by `NavSuiteSceneDecoratorStrategy`
  (`app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/NavSuiteSceneDecorator.kt`).
  It is a Navigation 3 `SceneDecoratorStrategy` that wraps a scene with the bar/rail
  *only if* the scene's entry metadata carries the `ShowNavBarKey` marker.
- A destination opts in per entry:
  `entry<Key>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) { … }`.
- Today the opted-in set is: the 5 tab roots (`HomeKey`, `InsurancesKey`, `ForeverKey`,
  `PaymentsKey`, `ProfileKey`) plus 4 deeper screens (`InsuranceContractDetailKey`,
  `TerminatedInsurancesKey`, `EurobonusKey`, `ClaimHistoryKey`).
- The highlighted tab is resolved **positionally** by the back stack, not from the key in
  isolation: `BackstackController.currentTopLevel` / `owningTopLevelTabForContentKey`
  (`app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt`).
  This already works for any screen inside the runs model, regardless of whether it shows
  the bar. So showing chrome deeper is purely a policy change — no infrastructure change
  is required.
- The lone-deep-link gate (`loneDeepLinkChrome`) is unaffected: a screen opened alone as a
  deep link still resolves chrome the same way it does today.

## Decisions

- **Keep the explicit opt-in model.** The bar stays off by default; we deliberately add
  eligibility to chosen screens. No flow can accidentally gain a bar.
- **Mechanism: inline metadata (Option A).** Add
  `metadata = NavSuiteSceneDecoratorStrategy.showNavBar()` to each chosen `entry<…>`,
  identical to how contract-detail / eurobonus / claim-history already opt in. No new
  abstraction (a key marker interface was considered and rejected as premature — the
  decorator gate reads scene metadata, not the key).

## Scope: screens to opt in (first batch)

In `app/feature/feature-payments/src/main/kotlin/com/hedvig/android/feature/payments/navigation/PaymentsEntries.kt`:
- `PaymentDetailsKey`
- `PaymentHistoryKey`
- `DiscountsKey`
- `MemberPaymentDetailsKey`

In `app/feature/feature-profile/src/main/kotlin/com/hedvig/android/feature/profile/tab/ProfileEntries.kt`:
- `ContactInfoKey`
- `InformationKey`
- `LicensesKey`
- `SettingsKey`
- `CertificatesKey`

**Explicitly excluded:** `ManualChargeKey` / `ManualChargeSuccessKey` (a payment action
with a success step — a mini-flow).

## The change

For each of the 9 entries above, add the metadata argument:

```kotlin
entry<PaymentDetailsKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) { key ->
  …
}
```

Both files already import `NavSuiteSceneDecoratorStrategy` and already use this pattern on
the tab root, so no new imports are needed.

No changes to the decorator, the back-stack controller, the runs model, tab resolution, or
deep-link gating.

## Primary risk: visual collisions

Behavior is safe (tab highlighting and the top-back-arrow + bottom-bar combination are
exactly what contract detail does today). The real risk is **layout**: the decorator hands
the destination the space *above* the bar and consumes the bottom system-bar inset. A
screen with a bottom-pinned CTA/button will now sit above the bar rather than at the screen
edge. Scrollable content is unaffected.

Therefore this is a verify-in-app change, not a code-only change.

## Verification

Run the app and, for each of the 9 screens:
1. Confirm the correct tab is highlighted (Payments screens → Payments; Profile/Certificates
   screens → Profile).
2. Confirm no bottom-anchored content (CTAs, buttons) collides with or is hidden behind the
   bar.
3. Confirm tapping the owning tab pops that tab's run back to its root, and tapping a
   different tab switches cleanly.
4. Confirm the top back-arrow still pops one entry as before.

If any screen has a bottom CTA that collides, note it; the per-screen fix (or dropping that
screen from the batch) is decided during implementation.

## Known tradeoff (accepted, not solved)

Opt-in eligibility is scattered across feature modules with no central registry. Acceptable
for an experiment. Revisit (e.g. a key marker interface, or flipping to opt-out) only if the
eligible set grows large.

## Out of scope

- Runtime feature flag / A/B experiment plumbing.
- Flipping the default to opt-out.
- Any change to the manual-charge screens or to flow screens (claim, termination, document
  generation).
