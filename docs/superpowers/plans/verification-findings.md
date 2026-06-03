# Nav2 → Nav3 Migration — Verification Findings Ledger

**Run started:** 2026-06-03 (autonomous, ~4h window)
**Branch:** `eng/metro-nav3-pr2-nav2-to-nav3` @ `cdebce653b`
**Plan:** `docs/superpowers/plans/2026-06-03-nav3-migration-verification.md`

Status legend: **PASS** | **FAIL** (blocking) | **GAP** (missing verification asset, non-blocking) | **FLAG** (needs human decision) | **N/A**.

---

## Behavior oracle (Phase 0.2 — the expected Nav2 semantics)

The single flat home-pinned back stack must reproduce these:
- **Forward push:** screen callback → `backstack.add(key)` → appends to active run.
- **Up:** `navigateUp()` pops the top entry.
- **System-back @ drill-down:** pops one entry.
- **System-back @ side-tab root:** collapses all side runs → returns to **Home** (never exits, never falls into another side tab).
- **System-back @ Home root:** exits the app.
- **Tab re-tap (current tab):** pops the top run to its start key (tab → its root).
- **Tab switch (other side tab):** moves that tab's run to the top, Home stays at base, other parked runs keep relative order; absent run appends fresh `[tabStartKey]`.
- **Select Home from a side tab:** collapse to Home in the state Home was last in.
- **Process death / config change:** the single list is `rememberSerializable`, so back stack + active tab restore exactly.

---

## Findings log

<!-- append TASK <id> — STATUS — note (evidence path) lines below -->

- TASK 0.1 — PASS — branch `eng/metro-nav3-pr2-nav2-to-nav3` @ cdebce653b, working tree clean (only the plan file untracked). Schema was MISSING; download kicked off (bg bdfxvn0yv).
- TASK 0.3 — PASS — ledger created.
- TASK 1.1 — PASS — `:app` debug compile succeeded (Phase 1.1).
- TASK 3 (serialization completeness) — PASS (after test fix) — production merged module registers **103** distinct concrete `HedvigNavKey` subtypes; **0 duplicate registrations** (line 173 ok), **0 serial-name collisions** (line 188 ok), all 103 resolve a working polymorphic serializer, and all 18 publicly-constructible keys round-trip. The agent-authored `ExhaustiveBackstackSerializationTest` hard-coded `expectedRegisteredSubtypeCount = 96` (an undercount from the manual navkey-inventory) → 2 spurious count failures (expected 96 but was 103). Corrected the constant to 103 and reworded the doc-comment to describe it as a live regression guard. Source-level completeness cross-check delegated to a read-only inventory subagent (in progress) to confirm no declared-but-unregistered key exists beyond the 103.
- TASK 5 (deep-link parity) — **REAL BUG, FIXED** — `HedvigDeepLinkMatcher.match()` (navigation-compose/commonMain) documented "Treat any throw as a non-match" but only handled a `null` return (`?: return@forEach`); it never caught the throw. Nav3's `UriDeepLinkMatcher.match()` THROWS `MissingFieldException` when a URI's path matches a pattern carrying a **required (no-default) arg** but the arg is absent. So a deep link like `hedvig://contract` (path-matches `/contract?contractId={contractId}` → ContractDetailKey) or `hedvig://claim-details` (→ ClaimDetailsKey) would crash instead of falling back to the browser / a lower-priority exact match. Agent-authored `HedvigDeepLinkMatcherTest` caught it (`contract_withoutId_fallsBackToInsurances`, `claimDetails_missingRequiredArg_doesNotMatch`). Fix: wrap the per-matcher call in `runCatching { matcher.match(request) }.getOrNull()` — realizes the documented intent. Re-test in progress (job bz4u4zlw0).
- TASK 7 (reachability) — **2 FAILS, NOT yet fixed** —
  - `HelpCenterKey`: `HedvigNavHost.kt:177` does `backstack.add(HelpCenterKey)`, but `HelpCenterGraph.kt:45` only registers `entry<HelpCenterHomeKey>` — pushing `HelpCenterKey` (no matching entry) is fatal in Nav3. Deep link path is fine (it targets `HelpCenterHomeKey`); only the in-app button is broken. Fix: push `HelpCenterHomeKey`.
  - `SettingsGraphKey`: `ProfileGraph.kt:73` does `backstack.add(SettingsGraphKey)`, but only `entry<SettingsKey>` exists (`ProfileGraph.kt:137`). Both are Nav2 graph-marker holdovers. Fix: push `SettingsKey`.
  - Both are leftover Nav2 "graph-start" marker keys that have no Nav3 entry. (Tasks #49, #50.)
  - **FIXED (both):** collapsed each redundant Nav2 graph-marker into its real screen key.
    - `SettingsGraphKey` (all in feature-profile): `ProfileGraph.kt` now pushes `SettingsKey`; removed its import, `subclass()` registration, and declaration. Clean, single-module.
    - `HelpCenterKey`/`HelpCenterHomeKey`: `HelpCenterKey` was the public cross-module target with no entry; `HelpCenterHomeKey` was `internal` (so `:app` couldn't push it) but held the entry + deep-link target. Collapsed to the public `HelpCenterKey`: graph now `entry<HelpCenterKey>`, deep link repointed to `HelpCenterKey.serializer()`, cross-sell permitting-list updated, `HelpCenterHomeKey` declaration + registration deleted. `HedvigNavHost.kt:177`'s existing `backstack.add(HelpCenterKey)` is now valid.
  - **Reachability re-swept after fixes** (simple-name diff of every `backstack.add(...)`/`navigateAndPopUpTo<>` push vs every `entry<>`): **0 remaining pushed-key-without-entry**. Verified `feature-payments`' own `internal ForeverKey` is self-consistent (it has its own `entry<ForeverKey>` at PaymentsGraph.kt:115 — a payments-local promo screen, distinct class from the Forever tab; not a bug).
- TASK 3b (serialization reconciliation) — NOTE — the runtime registry holds **103 distinct registered KClasses → reduced to 101 after the two collapses above** (each removed one registered subtype). A source inventory found these map to fewer *distinct simple names*: **6 simple-name groups are shared across modules** — `ForeverKey` (forever + payments), `SummaryKey` (moving-flow + addon-purchase + choose-tier), `ShowCertificateKey` (insurance-cert + travel-cert), `FirstVetKey` (help-center + home), `SubmitFailureKey` (addon-purchase + choose-tier), `SubmitSuccessKey` (addon-purchase + choose-tier). All are **distinct classes in distinct packages**, so their default serial names (fully-qualified) don't collide and round-trips are clean. **LATENT RISK:** if anyone adds a short `@SerialName("SummaryKey")`/`@SerialName("ForeverKey")` etc. to two of these, they'd silently collide and corrupt process-death restore. The `no two registered subtypes share a serial name` test guards exactly this — keep it.
- TASK 4 (backstack semantics) — PASS — all backstack/run-logic unit tests green on **both jvm and iosSimulatorArm64**:
  - `TopLevelRunLogicTest` 6/6 — covers the Phase 0.2 oracle at the pure-function level: `topLevelGraphOrNull` mapping, `nearestTopLevelGraph` (owner-of-top), `moveRunToTop` append-when-absent AND move-existing-run-preserving-Home-base-and-other-order (tab switch), `collapseToHome` keeps Home run / discards side runs (collapse-to-Home), `popTopRunToStart` drops top-run drilldowns (tab re-tap → root).
  - `HedvigNavBackstackTest` 8/8, `BackstackSerializationTest` 2/2, `ExhaustiveBackstackSerializationTest` 4/4, `HedvigNavKeySavedStateTest` 1/1, `HedvigDeepLinkMatcherTest` 19/19.
  - **GAP CLOSED:** added `HedvigBackstackControllerTest` (9 cases) — exercises the controller end-to-end against the Phase 0.2 oracle: `handleBack` at drill-down (pop one) / side-tab-root (→ collapseToHome) / Home-root (→ false=exit); `selectTopLevel` re-tap-current (→ pop run to root), switch-other-tab (→ moveRunToTop append), Home-from-side (→ collapse); `setLoggedIn`/`setLoggedOut`; derived `currentTopLevel`/`isLoggedIn`. Plus `DeepLinkNavigationTest` (5 cases) covers the deep-link push dedup. All 14 green on jvm.
- TASK 1.3 (ktlint) — PASS — `:navigation-compose :feature-profile :feature-help-center :app` ktlintCheck all BUILD SUCCESSFUL; the edits are format-clean (re-confirmed after the TASK 6 fix + 2 new test files).
- TASK 1.2 (lint) — PASS — `:app:lintDebug` BUILD SUCCESSFUL (re-run after the TASK 6 fix); no new lint errors introduced.
- TASK 7b (deep-link TARGET reachability — new exploration) — PASS — distinct from the `backstack.add` sweep: every key a deep link *resolves to* is navigated-to and rendered, so it must have an `entry<>` or opening that link crashes. Audited all **27** `uriDeepLinkMatchers(container.X, SomeKey.serializer())` targets against every `entry<>` registration: **all 27 have exactly one matching entry, 0 reachable crashes.** Same-simple-name trap checked: of the 6 reused names only `ForeverKey` is a deep-link target, and it correctly resolves to `feature-forever`'s `ForeverKey` (matcher + entry same package); the `feature-payments` `ForeverKey` is a separate non-deep-link class. (Test-only `Test*` keys in `HedvigDeepLinkMatcherTest` correctly excluded.)
- TASK 7c (entry-WITHOUT-serializer-registration — new exploration, mirror of 7b) — PASS — the inverse crash class: a key that has an `entry<>` (renderable + pushable) but NO `subclass(ThatKey::class)` in any `polymorphic(HedvigNavKey::class)` block would crash on process-death *save*, not on render. Audited set-difference A\B where A = all `entry<>` key types, B = all `subclass(...)` key types: **A\B is EMPTY** — every renderable key is registered for serialization (94 distinct entry simple-names ⊆ 96 subclass simple-names; the 2-key surplus is the two test-only `TestHomeKey`/`TestDetailKey` fixtures). All 6 reused-simple-name groups verified self-consistent within their own module (entry + subclass co-located per module). So render-reachability (7b) AND persist-reachability (7c) are both complete in both directions.
- TASK 5b (matcher fix on native) — PASS — `:navigation-compose:iosSimulatorArm64Test` + `:jvmTest` BUILD SUCCESSFUL after the TASK 5 commonMain `runCatching` fix — the deep-link matcher fix holds on Kotlin/Native, not just JVM.
- TASK 11 (full regression) — PASS — entire `:app:testDebugUnitTest` suite BUILD SUCCESSFUL after all fixes (not just the navigation subset) — the TASK 5/6/7 edits + 2 new controller/deep-link test files broke nothing else in `:app`.
- Cleanup — dead `SettingsGraphKey` `data object` declaration removed from `ProfileDestinations.kt:26` (was unregistered + unpushed after the TASK 7 collapse); `:feature-profile:compileDebugKotlin` still BUILD SUCCESSFUL.
- TASK 6 (duplicate-key crash via deep-link push) — **REAL BUG, FIXED** — `DeepLinkFirstUriHandler.openUri()` (`app/app/.../urihandler/DeepLinkFirstUriHandler.kt:22`) did a raw undeduped `backstack.add(destination)` on every resolved deep link. Nav3's `NavDisplay` wraps each entry in Compose `key(contentKey){}` with `contentKey = defaultContentKey(key) = key.toString()`, so two value-equal keys → "key X used multiple times" crash. Tab roots `HomeKey`/`InsurancesKey`/`ForeverKey`/`PaymentsKey`/`ProfileKey` are `data object`s AND deep-link targets. **Repro:** logged in, on Home (stack `[HomeKey]`), fire a Home deep link → `[HomeKey, HomeKey]` → crash. Same for re-firing any already-present deep-link key (e.g. a drill-down `data object`). The bottom-nav path was always safe (`selectTopLevel`/`moveRunToTop`/`collapseToHome` never blind-append). **Fix:** added `HedvigBackstackController.navigateToDeepLink(key)` — tab roots route through `selectTopLevel(topLevelGraph)` (deduped tab switch); any other key already on the stack is moved to top (remove-then-add) instead of re-appended; a genuinely new key just appends. `DeepLinkFirstUriHandler` now takes the controller instead of the raw `MutableList`; `HedvigApp.kt:127` passes `backstackController`. Regression test `DeepLinkNavigationTest` (5 cases) covers current-tab re-fire, present-side-tab re-fire, present-non-tab re-fire, new-non-tab append, absent-side-tab switch.
- TASK 1.x (native build) — **FLAG (pre-existing, NOT nav-migration)** — `:feature-help-center:compileKotlinIosSimulatorArm64` FAILS with a single error: `nativeMain/.../data/PuppyGuideAvailability.kt:14 'public' property exposes its 'internal' type 'GetPuppyGuideUseCase'`. This file is in-progress puppy-guide KMP work, untouched by the nav migration and not in this change set. It breaks the Kotlin/Native compile of feature-help-center (and therefore likely the umbrella native build — see [[project_umbrella_native_ci_blindspot]]). Flagged for the puppy-guide author; not fixed here (out of scope, risks conflicting with mid-flight work).

---

## Consolidated findings (filled at Phase 10)

### Blocking (FAIL)

None remaining. Three real, reachable Nav3 crash/correctness bugs were found and **fixed in this change set**, each with a regression test:

1. **Deep-link matcher crash** (TASK 5) — `HedvigDeepLinkMatcher.match()` didn't catch the `MissingFieldException` Nav3 throws when a URI path-matches a pattern with an absent required arg (e.g. `hedvig://contract`, `hedvig://claim-details`). Fixed with `runCatching{…}.getOrNull()`. Guarded by `HedvigDeepLinkMatcherTest` (19 cases, jvm+ios).
2. **Two unreachable push targets** (TASK 7) — `backstack.add(HelpCenterKey)` and `backstack.add(SettingsGraphKey)` pushed Nav2 graph-marker keys that had no Nav3 `entry<>` (fatal). Collapsed each marker into its real screen key. Re-swept: 0 pushed-key-without-entry remain.
3. **Duplicate value-equal key crash** (TASK 6) — `DeepLinkFirstUriHandler` raw-appended every resolved deep link; re-firing a deep link to a key already on the stack (notably tab-root `data object`s) produced two entries with the same `key.toString()` contentKey → "key … used multiple times". Fixed via `HedvigBackstackController.navigateToDeepLink()` (dedups; routes tab roots through `selectTopLevel`). Guarded by `DeepLinkNavigationTest` (5 cases).

### Flags (need human decision)

- **Pre-existing Kotlin/Native compile break, NOT this migration** (TASK 1.x) — `feature-help-center/nativeMain/.../PuppyGuideAvailability.kt:14` exposes an `internal` type (`GetPuppyGuideUseCase`) from a `public` property, failing `:feature-help-center:compileKotlinIosSimulatorArm64` (and likely the umbrella native build — see [[project_umbrella_native_ci_blindspot]]). It is in-flight puppy-guide KMP work, untouched by and unrelated to the nav migration. Left for the puppy-guide author; fixing here risks conflicting with mid-flight work. **This is the only thing standing between the branch and a green native build.**

### Gaps (non-blocking follow-ups)

- **Latent serial-name collision risk** (TASK 3b) — 6 simple-name groups (`ForeverKey`, `SummaryKey`×3, `ShowCertificateKey`, `FirstVetKey`, `SubmitFailureKey`, `SubmitSuccessKey`) are distinct classes in distinct packages, so their default (fully-qualified) serial names don't collide today. If anyone ever adds a short `@SerialName("SummaryKey")` to two of them, process-death restore silently corrupts. The `no two registered subtypes share a serial name` test in `ExhaustiveBackstackSerializationTest` guards exactly this — keep it.
- **No instrumented/Compose UI test of `NavDisplay` rendering** — all crash analysis here is at the unit/pure-function level. The contentKey-collision and unhandled-key crashes were reasoned through nav3 source + guarded by unit tests, but there is no on-device test that actually renders the stack. Phase 8 manual on-device matrix (below) covers this by hand.
- **Recents/URL-sharing (`onProvideAssistContent`)** intentionally dropped in this PR — reinstate Nav3-native in a follow-up (see [[project_recents_url_sharing_deferred]]). Not a regression.

### Phase 8 — manual on-device matrix (for the human, can't be unit-tested)

Run logged-in unless noted. Each row should NOT crash and should match the stated result:
1. Home → drill into a screen → system-back → returns one level. ✓ expected (handleBack pop).
2. Switch Home → Insurances → Profile via bottom bar → back → collapses straight to Home (no wandering through Insurances). ✓ (collapseToHome).
3. On Insurances with a drill-down, re-tap Insurances tab → pops to Insurances root. ✓ (popTopRunToStart).
4. From Home, fire `hedvig://home` deep link (or re-fire the current screen's deep link) → **no crash, no duplicate** (this is the TASK 6 fix — the highest-value manual check).
5. Fire `hedvig://contract` (no id) and `hedvig://claim-details` (missing arg) → falls back to browser / lower-priority match, **no crash** (TASK 5 fix).
6. Tap the in-app Help Center entry point and the Settings entry point → both open (TASK 7 fix).
7. Process death: background the app deep in a side-tab drill-down, kill via adb, relaunch → exact stack + active tab restored (rememberSerializable).
8. Config change (rotate) at several depths → stack preserved.

### Bottom line

The Nav2→Nav3 migration is **sound**. Verification found **3 real reachable bugs** (1 deep-link crash, 2 reachability crashes) plus **1 duplicate-key crash**, **all now fixed and regression-tested**. Serialization for process-death restore is complete (101 registered subtypes, 0 duplicate registrations, 0 serial-name collisions). Backstack run-logic and the controller are fully unit-tested against the Phase 0.2 oracle (TopLevelRunLogicTest 6, HedvigBackstackControllerTest 9, DeepLinkNavigationTest 5, serialization 6, deep-link matcher 19). Reachability is verified in every direction: 0 `backstack.add` pushes without an entry (TASK 7), all 27 deep-link target keys have entries (TASK 7b), and 0 renderable keys missing a serializer registration (TASK 7c) — so neither render nor process-death-save can crash on an orphan key. The full `:app` unit-test suite, lint, and ktlint all pass after every fix; the deep-link matcher fix is confirmed on Kotlin/Native (iOS) too.

This covers all known Nav3 crash classes: unhandled key on push (7/7b), value-equal duplicate contentKey (6), deep-link matcher throw (5/5b), process-death-save of an unregistered key (7c), and serial-name collision (3/3b). **One non-migration blocker remains for the native build** (PuppyGuideAvailability.kt — flagged to its author). Remaining work is the manual on-device matrix above (Phase 8) which cannot be unit-tested. **Nothing has been committed** — all fixes await review.
