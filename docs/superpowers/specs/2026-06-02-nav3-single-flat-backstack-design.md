# Nav3 Single Flat Home-Pinned Back Stack + Process-Death Persistence — Design

**Date:** 2026-06-02
**Branch context:** `eng/metro-nav3-pr2-nav2-to-nav3` (PR2 of the stacked migration), base `develop`.

## Problem

The current Nav3 back-stack design (`HedvigTopLevelBackStacks`) keeps one independent
`SnapshotStateList<HedvigNavKey>` per top-level tab, plus a separate login list, and exposes a single
`ForwardingBackStack` that delegates every `MutableList` operation to whichever list is currently
active. This has two problems:

1. **Back behavior regression / divergence.** With per-tab stacks, pressing system-back inside a
   non-Home tab drains that tab and then *exits the app*, instead of returning to Home first. The
   original Nav2 app returned to Home before exiting. We want the Google-recommended "fixed start
   destination" model: back drains the active drill-down, then a non-Home tab root returns to Home,
   and only Home-root back exits the app.
2. **No process-death safety.** The per-tab lists are plain `mutableStateListOf`, so the whole
   navigation state is lost on process death. We want the back stack to survive process death and
   configuration change.

## Locked decisions (already approved)

1. **Back from a non-Home tab root → "Return to Home, then exit"** (drain the active tab's
   drill-downs, then fall to Home; Home-root back exits the app). This is a behavior change vs the
   current branch and restores the original Nav2 behavior.
2. **Scope: single flat list, Home-pinned.** Adopt Google's official `common-ui` recipe (one flat
   list with contiguous per-destination runs), with Home pinned at the base. Delete
   `ForwardingBackStack`/`MutableListDelegate`. Derive `currentTopLevel` from the stack contents.

## Architecture

### One real list

There is exactly **one** `SnapshotStateList<HedvigNavKey>` for the whole app. `NavDisplay` renders it
directly, and every feature graph mutates it directly via the `backStack` reference they already
capture (`backStack.add(...)`, `popUpTo<…>`, `navigateAndPopUpTo<…>`, `navigateUp`, etc.). No
forwarding indirection — the list the graphs hold *is* the list `NavDisplay` renders.

States of the single list:

- **Logged out:** `[LoginKey, …login drill-downs]`. `LoginKey` is the base.
- **Logged in:** `[HomeKey, …]` with Home pinned at index 0. Above Home sit zero or more contiguous
  *runs*, one per visited top-level tab.

### Runs

A **run** is a maximal contiguous block that starts at a top-level tab key and extends up to (but
not including) the next top-level tab key. The five top-level tab keys are `HomeKey`,
`InsurancesKey`, `ForeverKey`, `PaymentsKey`, `ProfileKey`.

Invariants:

- Each top-level tab key appears **at most once** in the stack. Tab keys are only ever introduced by
  `selectTopLevel` (which moves an existing run or appends a fresh one); feature graphs only ever
  `add(...)` non-tab (drill-down) keys. The few graphs that need to land on a tab call
  `navigateToTopLevelGraph(...)` (→ `selectTopLevel`), never `add(HomeKey)`.
- When logged in, **Home's run is always at the base** (`HomeKey` at index 0). `selectTopLevel`
  never moves Home's run off the base.
- The **owning tab** of any entry is the nearest tab key at or below it. `currentTopLevel` is the
  owning tab of the topmost entry, i.e. the tab of the last (top) run.

### Operations on the single list

All mutating operations run inside `Snapshot.withMutableSnapshot { … }` and mutate the list **in
place** (so the captured `backStack` reference and `NavDisplay` both observe the final state).

- **`add(key)`** (used by feature graphs, unchanged): append to the end. The currently-active run
  grows.

- **`selectTopLevel(tab)`** (rail/bar tap):
  - If `tab == currentTopLevel`: pop the **top run to its start key** (re-tap pops the current tab to
    its root). No-op when the run is already just its root.
  - Else if `tab == Home`: **collapse to Home** — truncate to Home's run, discarding all parked
    side-tab runs. (Selecting Home from a side tab returns to Home in the state Home was last in.)
  - Else (switch to a different side tab): **move that tab's run to the top**, preserving Home's run
    at the base and the relative order of the other parked runs. If the tab has no run yet, append a
    fresh `[tabStartKey]` run to the top.

- **`handleBack(): Boolean`** (system back / `NavDisplay.onBack`; returns `false` ⇒ caller finishes
  the app):
  - `size <= 1` → return `false` (at `[Home]` or `[Login]`, exit the app).
  - Top entry is a **non-Home tab root** → **collapse to Home** (discard all parked side-tab runs),
    return `true`. This is the "no wandering" rule: backing out of a side tab returns to Home, never
    falls into a previously-active side tab.
  - Otherwise (a drill-down on top, or any login-flow entry) → pop the top entry, return `true`.

- **`setLoggedIn()`**: clear the list, add `HomeKey`.
- **`setLoggedOut()`**: clear the list, add `LoginKey`.

### Derived state

- **`isLoggedIn: Boolean`** = the base entry is a top-level tab key
  (`backStack.firstOrNull()?.topLevelGraphOrNull() != null`). Logged-out base is `LoginKey`
  (`topLevelGraphOrNull() == null`).
- **`currentTopLevel: TopLevelGraph`** = `topLevelGraphOrNull` of the nearest tab key at or below the
  top entry; falls back to `Home` when logged out (it is only consulted while `isLoggedIn`).
- **`currentDestination: HedvigNavKey?`** = `backStack.lastOrNull()` (unchanged).

These replace the previously stored `isLoggedIn` / `currentTopLevel` fields — the single list is the
sole source of truth, so process-death restore reconstructs them for free.

### Pure, Compose-free run logic

The run math lives in a separate file as pure functions over `List<HedvigNavKey>`, unit-tested
without Compose:

- `HedvigNavKey.topLevelGraphOrNull(): TopLevelGraph?` — reverse of `TopLevelGraph.startDestination`
  (`HomeKey → Home`, `InsurancesKey → Insurances`, `ForeverKey → Forever`, `PaymentsKey → Payments`,
  `ProfileKey → Profile`, else `null`).
- `nearestTopLevelGraph(stack): TopLevelGraph?` — scan from the end for the first entry whose
  `topLevelGraphOrNull()` is non-null.
- `moveRunToTop(stack, tab): List<HedvigNavKey>` — move `tab`'s run (or append `[tab.startKey]` if
  absent) to the end, preserving the relative order of the remaining runs.
- `collapseToHome(stack): List<HedvigNavKey>` — truncate to `[0, firstSideRunStart)`, where
  `firstSideRunStart` is the first index `> 0` whose entry is a tab key. Keeps Home + Home's
  drill-downs, discards all side runs.
- `popTopRunToStart(stack): List<HedvigNavKey>` — truncate to `[0, topRunStart]`, where `topRunStart`
  is the last index whose entry is a tab key. Keeps the top run's root, drops its drill-downs.

`HedvigTopLevelBackStacks` calls these and applies the result in place
(`withMutableSnapshot { clear(); addAll(result) }`).

## Process-death persistence

The single list is created with `androidx.compose.runtime.saveable.rememberSerializable` (the same
API Nav3's own `rememberNavBackStack` uses under the hood), so it survives process death and
configuration change:

```kotlin
val backStack = rememberSerializable(
  configuration = savedStateConfiguration,
  serializer = SnapshotStateListSerializer(PolymorphicSerializer(HedvigNavKey::class)),
) {
  mutableStateListOf<HedvigNavKey>(LoginKey)
}
```

- `SnapshotStateListSerializer` is from `androidx.savedstate.compose.serialization.serializers`.
- `SavedStateConfiguration` is from `androidx.savedstate.serialization`.
- `HedvigNavKey` is a plain (non-sealed) interface spanning ~25 feature modules, so polymorphic
  serialization requires every concrete `HedvigNavKey` subtype to be **registered** in the
  `SerializersModule`. Reflection-based registration is not used.

### Per-feature registration (Metro multibinding)

Each feature module contributes a `SerializersModule` registering its own `HedvigNavKey` subtypes,
following the existing `DeepLinkMatcherProvider` pattern (`@ContributesIntoSet(AppScope::class)`):

```kotlin
@ContributesIntoSet(AppScope::class)
@Provides
fun provideTravelCertificateSerializersModule(): SerializersModule = SerializersModule {
  polymorphic(HedvigNavKey::class) {
    subclass(TravelCertificateKey::class)
    subclass(TravelCertificateChooseContractKey::class)
    // … every HedvigNavKey subtype declared in this module
  }
}
```

`:app` injects the resulting `Set<SerializersModule>`, folds it onto the existing
`HedvigBaseSerializersModule` with the existing `Iterable<SerializersModule>.merge()` helper, and
feeds the result into a single `SavedStateConfiguration`:

```kotlin
@Inject private lateinit var serializersModules: Set<SerializersModule>
…
val savedStateConfiguration = SavedStateConfiguration {
  serializersModule = serializersModules.merge()
}
```

`MainActivity` passes this `SavedStateConfiguration` into `rememberHedvigTopLevelBackStacks(...)`.

The `merge()` helper and `HedvigBaseSerializersModule` already exist in
`navigation-compose/DestinationSerializersModule.kt` but are currently inert (zero callers); this
work makes them load-bearing. The doc comment there (which still references `rememberNavBackStack`)
is updated to describe the single-list `rememberSerializable` wiring.

### Modules that declare HedvigNavKey subtypes (23)

`feature-login` (LoginKey + SwedishLoginDestination etc.), `feature-home`, `feature-insurances`,
`feature-forever`, `feature-payments`, `feature-profile`, `feature-travel-certificate`,
`feature-payout-account`, `feature-chat`, `feature-chip-id`, `feature-claim-chat`,
`feature-claim-details`, `feature-claim-history`, `feature-connect-payment-trustly`,
`feature-delete-account`, `feature-edit-coinsured`, `feature-help-center` (KMP commonMain),
`feature-image-viewer`, `feature-insurance-certificate`, `feature-movingflow` (watch nested sealed
keys), `feature-addon-purchase`, `feature-remove-addons` (KMP androidMain), `feature-change-tier`,
`feature-terminate-insurance`. Each gets exactly one contribution registering all of its
`HedvigNavKey` subtypes. (The exact module count is finalized during the plan by grepping for
`: HedvigNavKey` per module.)

## Components changed

- **Rewrite:** `app/app/.../navigation/HedvigTopLevelBackStacks.kt` — single list, derived state, new
  ops; delete `ForwardingBackStack`/`MutableListDelegate`; `rememberHedvigTopLevelBackStacks` takes a
  `SavedStateConfiguration` and uses `rememberSerializable`.
- **New:** `app/app/.../navigation/TopLevelRunLogic.kt` (or similar) — pure run functions +
  `topLevelGraphOrNull`. Plus a unit test file.
- **Modify:** `app/app/.../ui/HedvigAppState.kt` — `currentTopLevelGraph` and `isTopLevelStartDestination`
  consume the derived values / `topLevelGraphOrNull`.
- **Modify:** `app/app/.../navigation/HedvigNavHost.kt` — `onBack` routes through `handleBack()`;
  `popBackStackOrFinish` unchanged for graph-initiated pops (it stays a plain pop-or-finish; only the
  *system back* gets the collapse-to-Home rule). `NavDisplay` is fed the single list.
- **Modify:** `app/app/.../MainActivity.kt` — inject `Set<SerializersModule>`, build
  `SavedStateConfiguration`, pass it to `rememberHedvigTopLevelBackStacks`.
- **New (×~23):** one `*SerializersModule` contribution per feature module declaring `HedvigNavKey`
  subtypes, each with a round-trip unit test.
- **Modify:** `navigation-compose/.../DestinationSerializersModule.kt` — fix the doc comment.

## Testing

- **Pure run logic:** unit tests (no Compose/Android) covering `topLevelGraphOrNull`,
  `nearestTopLevelGraph`, `moveRunToTop` (absent run appends; present run reorders; Home preserved at
  base; relative order of other runs preserved), `collapseToHome`, `popTopRunToStart`, and the
  `handleBack`/`selectTopLevel` decision logic (extracted into pure form where practical).
- **Per-feature SerializersModule:** each feature gets a round-trip test (serialize each key with the
  module → JSON → deserialize → assert equality) to catch a forgotten `subclass(...)` registration.
- **Manual on-device:** drill into a side tab, switch tabs via the rail (state preserved), system-back
  from a side-tab root (returns to Home, discards parked runs), Home-root back (exits app); then a
  process-death round-trip (developer option "Don't keep activities") confirming the back stack and
  active tab restore.

## Out of scope

- Recents/URL sharing reinstatement (tracked separately).
- Any change to the per-feature graph builders beyond what the new `backStack` semantics require
  (they already mutate a `MutableList<HedvigNavKey>` and are unaffected).
