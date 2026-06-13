# Lone-Deep-Link Navigation Chrome Fix — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stop the navigation suite (tab bar/rail) from appearing over a deep-link stack that isn't rooted at Home, and make Up work from a foreign-hosted lone Home.

**Architecture:** The app has one navigation suite driven by `BackstackController.loneDeepLinkChrome`. The runs model (tab switching) assumes `HomeKey` sits at index 0 of `entries`; every helper in `TopLevelRunLogic.kt` silently no-ops otherwise. Today `loneDeepLinkChrome` gates the suite on stack **size** (`size == 1`), which diverges from that invariant: the moment a lone deep link grows past one entry the suite reappears over a non-Home base, and the Home tab becomes a dead no-op. We re-gate the suite on **runs-model membership** (our own task AND a Home/Login base) instead of size, and decide the suppressed-state affordance from the *top* entry. Separately, `navigateUp` is generalized so a foreign-hosted lone Home (no ancestry to rebuild) still escapes into our own task.

**Tech Stack:** Kotlin, Jetpack Compose runtime (snapshot state), JUnit + assertk. Unit tests only — `BackstackController` is constructed directly in tests with hand-built snapshot holders.

---

## Background the implementer needs

Read these before starting:

- `app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt` — the class under change. Note `isOwnTask: () -> Boolean` (defaults to `{ true }`; `MainActivity` attaches `{ isTaskRoot }`), `escapeToOwnTask: (List<HedvigNavKey>) -> Unit`, and the private `SnapshotStateList<HedvigNavKey>.replaceWith(...)` extension at the bottom of the file.
- `app/app/src/main/kotlin/com/hedvig/android/app/navigation/TopLevelRunLogic.kt` — `topLevelTabOrNull()` (internal extension, same package) and `syntheticStackFor(key)`. `syntheticStackFor(HomeKey)` returns `[HomeKey]` (size 1); `syntheticStackFor(InsurancesKey)` returns `[HomeKey, InsurancesKey]`.
- `app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/NavSuiteSceneDecorator.kt` — the `LoneDeepLinkChrome` enum (`ShowSuite` / `ShowUpBar` / `ShowNothing`) and how the decorator consumes it. No change needed here, but it shows the contract.
- `app/app/src/test/kotlin/com/hedvig/android/app/navigation/BackstackControllerTest.kt` — existing tests and the `controllerWith(vararg keys)` helper (defaults `isOwnTask` to `true`). Tests that explicitly need a foreign task construct `BackstackController(...)` directly with `isOwnTask = { false }`.

**Key facts that make this safe:**
- `HomeKey`, `InsurancesKey` are `TopLevelTabRoot` (so `topLevelTabOrNull()` is non-null). `HelpCenterKey` is **not** a tab root (so `topLevelTabOrNull()` is null) — the existing tests use it as the stand-in "deep, non-tab" key.
- All existing `loneDeepLinkChrome` and `navigateUp` tests use the default `isOwnTask = { true }` and are Home/Login-rooted or lone-non-Home, so the changes below keep every one of them green. The new behaviour is only reachable with `isOwnTask = { false }` or a grown non-Home stack — neither of which any current test exercises.

**Run a single test class with:**
```bash
./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.BackstackControllerTest"
```

---

## Task 1: Gate the suite on runs-model membership, not stack size

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt` (the `loneDeepLinkChrome` getter, currently lines ~130-143)
- Test: `app/app/src/test/kotlin/com/hedvig/android/app/navigation/BackstackControllerTest.kt`

- [ ] **Step 1: Write the failing tests**

Add these test methods to `BackstackControllerTest`. The first two are the regression that's currently broken; the last two are the foreign-task Home distinction.

```kotlin
@Test
fun `loneDeepLinkChrome stays suppressed when a lone tab root drills into a child`() {
  // Bug repro: a deep-linked tab root that pushes a child must NOT resurrect the suite — Home is
  // not at the base, so the runs model (and the Home tab) would silently no-op.
  val controller = controllerWith(InsurancesKey, HelpCenterKey)
  assertThat(controller.loneDeepLinkChrome).isEqualTo(LoneDeepLinkChrome.ShowNothing)
}

@Test
fun `loneDeepLinkChrome stays suppressed when a lone deep screen drills into a child`() {
  val controller = controllerWith(HelpCenterKey, InsurancesKey)
  // Top is a tab root with no own back affordance, but we are still outside the runs model.
  assertThat(controller.loneDeepLinkChrome).isEqualTo(LoneDeepLinkChrome.ShowUpBar)
}

@Test
fun `loneDeepLinkChrome is ShowUpBar for a foreign-hosted lone Home`() {
  val controller = BackstackController(
    mutableStateListOf(HomeKey),
    mutableStateMapOf(),
    mutableStateOf(null),
    mutableStateOf(null),
    isOwnTask = { false },
  )
  assertThat(controller.loneDeepLinkChrome).isEqualTo(LoneDeepLinkChrome.ShowUpBar)
}

@Test
fun `loneDeepLinkChrome is ShowSuite for a lone Home in our own task`() {
  val controller = BackstackController(
    mutableStateListOf(HomeKey),
    mutableStateMapOf(),
    mutableStateOf(null),
    mutableStateOf(null),
    isOwnTask = { true },
  )
  assertThat(controller.loneDeepLinkChrome).isEqualTo(LoneDeepLinkChrome.ShowSuite)
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run:
```bash
./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.BackstackControllerTest"
```
Expected: the two `stays suppressed ...` tests FAIL (current code returns `ShowSuite` for any `size > 1`), and `is ShowUpBar for a foreign-hosted lone Home` FAILS (current code returns `ShowSuite` for any `HomeKey` base regardless of task). `is ShowSuite for a lone Home in our own task` PASSES already.

- [ ] **Step 3: Replace the `loneDeepLinkChrome` getter**

In `BackstackController.kt`, replace the existing getter (the KDoc block plus the `get()` currently at lines ~130-143) with:

```kotlin
  /**
   * Drives the scene decorator (D11). The navigation suite (tab bar/rail) may render only while the
   * stack is inside our runs model: our own task AND rooted at [HomeKey] (or [LoginKey], which never
   * shows a suite anyway). A lone deep link — or any stack that grew from one before [navigateUp]
   * re-rooted it at Home — is *outside* that model. The runs helpers in `TopLevelRunLogic` assume
   * `HomeKey` at index 0 and silently no-op otherwise, so rendering the suite there strands the user
   * (a dead Home tap). Outside the model we suppress the suite and instead key off the *top* (rendered)
   * entry: a bare top-level root gets a decorator-supplied Up-bar (it has no back affordance of its
   * own), a deeper screen gets nothing (it draws its own Up). A lone Home hosted in a foreign task is
   * itself a top-level root, so it gets the Up-bar — letting the user escape back via Up (see
   * [navigateUp]).
   */
  val loneDeepLinkChrome: LoneDeepLinkChrome
    get() {
      val first = entries.firstOrNull()
      val insideRunsModel = isOwnTask() && (first is HomeKey || first is LoginKey)
      if (insideRunsModel) return LoneDeepLinkChrome.ShowSuite
      return if (entries.lastOrNull()?.topLevelTabOrNull() != null) {
        LoneDeepLinkChrome.ShowUpBar
      } else {
        LoneDeepLinkChrome.ShowNothing
      }
    }
```

`LoginKey` is already imported in this file; `topLevelTabOrNull()` is the internal extension from `TopLevelRunLogic.kt` in the same package and is already used by the old getter.

- [ ] **Step 4: Run the tests to verify they pass**

Run:
```bash
./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.BackstackControllerTest"
```
Expected: PASS, including the pre-existing `loneDeepLinkChrome is ShowSuite for lone Home, login, and multi-entry stacks` test (its three cases — `[Home]`, `[Login]`, `[Home, Insurances]` — are all own-task and Home/Login-rooted, so they still return `ShowSuite`).

- [ ] **Step 5: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt \
        app/app/src/test/kotlin/com/hedvig/android/app/navigation/BackstackControllerTest.kt
git commit -m "fix(nav): suppress nav suite outside the Home-rooted runs model

Gate loneDeepLinkChrome on runs-model membership (own task + Home/Login
base) instead of stack size, so a lone deep link that drills into a child
no longer resurrects the tab bar over a non-Home base (dead Home tap)."
```

---

## Task 2: Escape to our own task from a foreign-hosted lone Home on Up

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt` (the `navigateUp()` override, currently lines ~193-208)
- Test: `app/app/src/test/kotlin/com/hedvig/android/app/navigation/BackstackControllerTest.kt`

**Why:** With Task 1 a foreign-hosted lone Home shows an Up-bar, but its Up button calls `navigateUp()`, which today is a no-op at a lone Home: `syntheticStackFor(HomeKey)` is `[HomeKey]` (size 1), so the existing `synthetic.size > 1` branch is skipped and it falls through to a root pop that does nothing. We generalize so a lone leaf with no ancestry still escapes into our own task, seeded with the leaf itself — landing back on Home but now in our task, where `isOwnTask()` is true and the suite comes alive.

- [ ] **Step 1: Write the failing test**

Add to `BackstackControllerTest`:

```kotlin
@Test
fun `navigateUp at a foreign-hosted lone Home escapes to own task seeded with Home`() {
  var escaped: List<HedvigNavKey>? = null
  val controller = BackstackController(
    mutableStateListOf(HomeKey),
    mutableStateMapOf(),
    mutableStateOf(null),
    mutableStateOf(null),
    isOwnTask = { false },
    escapeToOwnTask = { escaped = it },
  )
  assertThat(controller.navigateUp()).isTrue()
  assertThat(escaped).isEqualTo(listOf(HomeKey))
  // The foreign-hosted stack is left untouched; the relaunched task owns the rebuilt root.
  assertThat(controller.entries.toList()).containsExactly(HomeKey)
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:
```bash
./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.BackstackControllerTest"
```
Expected: FAIL — current `navigateUp()` returns `false` for a lone Home and never calls `escapeToOwnTask`, so `escaped` stays `null` and `navigateUp()` is `false`.

- [ ] **Step 3: Replace the `navigateUp()` override**

In `BackstackController.kt`, replace the existing `navigateUp()` override (the KDoc block plus the function body currently at lines ~185-208) with:

```kotlin
  /**
   * Task-aware Up. For a lone deep link it materializes the parent ancestry — `[Home]` for a lone
   * tab root, `[Home, Insurances]` for a lone contract detail — so Up behaves exactly like Back would
   * have inside the app. When we were launched into a foreign task ([isOwnTask] is false) that parent
   * stack is handed to [escapeToOwnTask], which re-roots the app in its own task; otherwise it is
   * materialized in place. A lone leaf with no ancestry (e.g. Home) hosted in a foreign task has no
   * parent to rebuild, but must still escape so the runs model — and its nav bar — come alive; the
   * escape is seeded with the leaf itself. Everywhere else Up is a plain temporal pop, identical to
   * Back (a no-op at the root — it must not exit the app the way Back does).
   */
  override fun navigateUp(): Boolean {
    val top = entries.lastOrNull() ?: return false
    if (entries.size == 1) {
      val synthetic = syntheticStackFor(top)
      val parentStack = synthetic.dropLast(1)
      when {
        parentStack.isNotEmpty() -> {
          if (isOwnTask()) entries.replaceWith(parentStack) else escapeToOwnTask(parentStack)
          return true
        }

        !isOwnTask() -> {
          escapeToOwnTask(synthetic)
          return true
        }
      }
    }
    return super.popBackstack()
  }
```

`replaceWith` is the private `SnapshotStateList<HedvigNavKey>` extension at the bottom of this file.

- [ ] **Step 4: Run the tests to verify they pass**

Run:
```bash
./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.BackstackControllerTest"
```
Expected: PASS, including the pre-existing `navigateUp at the Home root returns false` (default `isOwnTask = { true }`, so `parentStack` is empty AND `!isOwnTask()` is false → falls through to a root pop returning `false`), `navigateUp on a lone side-tab root rebuilds to Home` (`parentStack = [Home]`, own task → in-place), and `navigateUp from a foreign-hosted lone deep link escapes to own task with the parent stack` (`parentStack = [Home]`, foreign → escape).

- [ ] **Step 5: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt \
        app/app/src/test/kotlin/com/hedvig/android/app/navigation/BackstackControllerTest.kt
git commit -m "fix(nav): escape to own task on Up from a foreign-hosted lone Home

A lone Home has no ancestry to rebuild, so navigateUp was a no-op there.
When hosted in a foreign task, escape into our own task seeded with Home
so the runs model and nav bar come alive."
```

---

## Task 3: Full-suite regression run and ktlint

**Files:** none (verification only).

- [ ] **Step 1: Run the navigation test suites**

Run:
```bash
./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.*"
```
Expected: PASS. This includes `TopLevelRunLogicTest`, `DeepLinkNavigationTest`, `SyntheticStackTest`, and the serialization tests — none of which we changed, but they share the navigation model and confirm no regression.

- [ ] **Step 2: Format**

Run:
```bash
./gradlew :app:ktlintFormat
```
Expected: no changes, or auto-formatting applied. If it changed files, re-run the test command from Step 1, then `git add` + amend the relevant task commit (or make a small `style:` commit).

---

## Manual verification (not automatable here)

These are device/emulator checks for whoever runs the app — the unit tests can't observe the rendered chrome. Worth doing before merge:

1. **The original bug:** deep-link (while logged out, then log in) to a contract detail so it lands alone → no tab bar. Drill into a child screen from there → tab bar still does **not** appear (previously it did, with a dead Home tab). Press the screen's own Up repeatedly → after `navigateUp` re-roots at `[Home, Insurances]`, the tab bar reappears and the Home tab works.
2. **Lone tab root:** deep-link to the Insurances tab alone → bare Up-bar, no rail. Up → back into our task at Home with the rail.
3. **Foreign-hosted lone Home:** tap a notification (or open a Home/claimFlow https link from another app) that resolves to Home → bare Up-bar on Home. Up → app re-roots in its own task on Home, now with the full tab bar. Re-landing on "the same" Home screen is expected and fine.
4. **Normal launch unaffected:** open the app normally → Home with the tab bar, all tabs work.

---

## Self-review notes

- **Spec coverage:** point 1 (Up only on top-level entries, deeper draw their own) → Task 1's top-entry `topLevelTabOrNull()` branch. Point 2 + snag B (foreign-hosted lone Home shows Up and escapes) → Task 1 (chrome) + Task 2 (action). The "invariant assertion" idea was dropped by decision: re-gating the suite on a Home/Login base means the suite (and thus the runs helpers) can only render when the invariant already holds, so a separate assertion is redundant.
- **Out of scope:** the unmatched-link → browser fallback for notification/external sources is a separate problem — see `docs/architecture/deep-link-browser-fallback-problem.md`. It touches adjacent code (`DeepLinkFirstUriHandler`, `MainActivity.handleDeepLinkIntent`) but neither change depends on the other.
- **Type consistency:** `loneDeepLinkChrome` returns `LoneDeepLinkChrome` (unchanged enum); `navigateUp(): Boolean` (unchanged signature); both reuse existing `isOwnTask`, `escapeToOwnTask`, `syntheticStackFor`, `topLevelTabOrNull`, and the private `replaceWith`.
