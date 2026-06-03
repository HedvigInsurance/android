# Per-Tab Back Stack Memory (Nav3) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore the Nav2 behavior where each top-level tab independently remembers its back stack (keys **and** per-screen saved state), so leaving a drilled-down tab for Home and later re-selecting that tab brings its whole run back — while system-back from Home still exits the app and predictive back stays correct everywhere.

**Architecture:** Keep the single flat `SnapshotStateList<HedvigNavKey>` as the *rendered* source of truth (so the 154 existing `backstack.add/popUpTo/...` call sites across feature modules are untouched). Add a side `parkedRuns` map that stashes a side-tab's run when you switch away instead of discarding it, and restores it when you return. The flat stack therefore always holds exactly `homeRun + (at most one active side-tab run)`, which keeps `currentTopLevel` derivable and keeps predictive back's preview honest. State retention for parked (off-screen) entries is achieved by **forking** Nav3's two state decorators so their disposal hook (`onPop`) only fires when a key is absent from the *union* of the rendered stack and all parked runs — not merely absent from the rendered stack.

**Tech Stack:** Kotlin, Jetpack Compose, Jetpack Navigation 3 (`androidx.navigation3`, `androidx.lifecycle.viewmodel.navigation3`), `androidx.savedstate` compose serializers, kotlinx.serialization, JUnit4 + assertk (pure-JVM unit tests).

---

## Background: why this works (read before implementing)

Three facts from the Nav3 sources drive the whole design. Verify them if anything below seems surprising:

1. **`NavDisplay` never mutates the back stack.** It renders the `List` you give it and, on a back gesture, calls your `onBack`. So "system back" and "controller navigation" are the same funnel — there is no path that pops state behind the controller's back. (`HedvigNavHost.kt` already wires `onBack = { controller.handleBack() }`.)

2. **Predictive back previews `renderedStack[lastIndex - 1]`.** The peek shown during a back swipe is whatever sits second-from-top in the rendered list. Because our flat stack is always `homeRun + activeSideRun`, the entry beneath any side-tab root is Home, beneath a drill-down is its parent — so the preview is always the true back destination. (This is why we keep the flat stack and do **not** split rendering across multiple lists.)

3. **State decorators dispose on `onPop(contentKey)`, which fires when a key leaves the rendered stack** (popped *and* left composition — see `DecoratedNavEntries.kt`). `contentKey` defaults to `key.toString()` (`NavEntry.kt:92`). The stock `SaveableStateHolderNavEntryDecorator` calls `saveableStateHolder.removeState(contentKey)`; the stock `ViewModelStoreNavEntryDecorator` clears the entry's `ViewModelStore`. **That disposal is exactly what kills parked-tab state today.** Our forked decorators gate `onPop` on union membership: a key that merely moved from the rendered stack into `parkedRuns` is still "live", so its `rememberSaveable` values, `SavedStateHandle`, and `ViewModel` survive and are reused when the run is restored.

### Invariants the implementation must preserve

- The flat `entries` list contains `homeRun` (HomeKey + its drill-downs) followed by **at most one** side-tab run. Other tabs' runs live in `parkedRuns`, never concatenated into the rendered stack.
- `currentTopLevel` is **derived** (`nearestTopLevelGraph(entries)`), never stored — there is only ever one side run in the rendered stack, so the top-most tab key unambiguously identifies the active tab. (This is why the earlier `mutableStateOf`-drift concern does not apply.)
- `parkedRuns` only ever holds **side-tab** runs (never Home; Home always lives in the rendered stack).
- The decorator "live set" = `entries` keys ∪ all `parkedRuns` values, mapped through `.toString()` to match `contentKey`.
- **Only a tab tap (`selectTopLevel`) ever parks a run; system back (`handleBack`) never does.** This matches Nav2 exactly: leaving a side tab by tapping *another* top-level destination (including Home) preserves the leaving tab's whole run **and** its per-screen saved state in `parkedRuns`; draining that same side tab with system back drops it **completely** — each key leaves the rendered stack, is absent from `parkedRuns`, so `allLiveContentKeys` no longer covers it and the decorators dispose its `rememberSaveable`/`SavedStateHandle`/ViewModel state. `handleBack` must therefore stay a plain pop — do **not** "helpfully" stash the run it drains, or a system-back-then-retap would wrongly resurrect state Nav2 would have thrown away.

---

## File Structure

| File | Responsibility | Action |
|---|---|---|
| `app/navigation/navigation-core/src/commonMain/kotlin/com/hedvig/android/navigation/core/TopLevelGraph.kt` | Tab enum; must be `@Serializable` to be a `parkedRuns` map key | Modify |
| `app/app/src/main/kotlin/com/hedvig/android/app/navigation/TopLevelRunLogic.kt` | Pure run-splitting helpers | Modify (add `activeSideRun`, remove `moveRunToTop`) |
| `app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt` | Owns rendered stack + parked runs; stash/restore on tab switch; exposes live-key union | Modify |
| `app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/RetainedSaveableStateHolderNavEntryDecorator.kt` | Forked saveable decorator gated on union membership | Create |
| `app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/RetainedViewModelStoreNavEntryDecorator.kt` | Forked ViewModelStore decorator gated on union membership | Create |
| `app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/HedvigNavDisplay.kt` | Wires the retained decorators; takes the live-key provider | Modify |
| `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt` | Passes `controller.allLiveContentKeys` to `HedvigNavDisplay` | Modify |
| `app/app/src/test/kotlin/com/hedvig/android/app/navigation/TopLevelRunLogicTest.kt` | Unit tests for run helpers | Modify |
| `app/app/src/test/kotlin/com/hedvig/android/app/navigation/BackstackControllerTest.kt` | Unit tests for controller behavior | Modify |
| `app/app/src/test/kotlin/com/hedvig/android/app/navigation/DeepLinkNavigationTest.kt` | Unit tests for deep-link routing (re-verify under new model) | Modify |

> **Naming note (codebase ground truth, verified 2026-06-03):** the controller class is **`BackstackController`** in **`BackstackController.kt`**, it implements the **`Backstack`** interface (`app/navigation/navigation-compose/.../Backstack.kt`) whose single list property is **`entries: MutableList<HedvigNavKey>`** — there is no `backstack`/`backStack` property. The composable factory is **`rememberHedvigBackstackController`** (unchanged). `HedvigNavDisplay` takes a **`Backstack`** (not a raw list) and internally renders `backstack.entries`. `HedvigNavHost` passes the controller directly (`backstack = hedvigAppState.backstackController`). All code below uses these real names.

---

## Phase 0 — Make `TopLevelGraph` serializable

`parkedRuns` is keyed by `TopLevelGraph`; persisting it across process death needs a `KSerializer` for the enum.

### Task 0: Annotate `TopLevelGraph` as `@Serializable`

**Files:**
- Modify: `app/navigation/navigation-core/src/commonMain/kotlin/com/hedvig/android/navigation/core/TopLevelGraph.kt`

- [ ] **Step 1: Read the current file**

Run: `cat app/navigation/navigation-core/src/commonMain/kotlin/com/hedvig/android/navigation/core/TopLevelGraph.kt`
Expected: `enum class TopLevelGraph { ... }` with no `@Serializable`.

- [ ] **Step 2: Add the annotation**

Add the import and annotation:

```kotlin
package com.hedvig.android.navigation.core

import kotlinx.serialization.Serializable

@Serializable
enum class TopLevelGraph {
  // ...existing entries unchanged...
}
```

Confirm the module applies the serialization plugin. `navigation-core/build.gradle.kts` should already have `hedvig { serialization() }`; if it does not, add it inside the existing `hedvig { }` block.

- [ ] **Step 3: Compile the module**

Run: `./gradlew :navigation-core:compileKotlinMetadata`
Expected: `BUILD SUCCESSFUL`. (If the module name differs, use `./gradlew :navigation:navigation-core:...`; resolve the path via `./gradlew projects | grep -i navigation-core`.)

- [ ] **Step 4: Commit**

```bash
git add app/navigation/navigation-core/src/commonMain/kotlin/com/hedvig/android/navigation/core/TopLevelGraph.kt
git commit -m "Make TopLevelGraph serializable for parked back-stack persistence"
```

---

## Phase 1 — Run-logic helper

We need a helper that returns the active side-tab run (to stash it). `moveRunToTop` becomes dead code once the controller stops concatenating side runs, so it (and its tests) are removed.

### Task 1: Add `activeSideRun`, remove `moveRunToTop`

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/TopLevelRunLogic.kt`
- Test: `app/app/src/test/kotlin/com/hedvig/android/app/navigation/TopLevelRunLogicTest.kt`

- [ ] **Step 1: Write the failing tests**

In `TopLevelRunLogicTest.kt`, **delete** the two `moveRunToTop_*` tests (lines 35-44) and **add**:

```kotlin
  @Test fun activeSideRun_isEmptyOnHome() {
    assertThat(activeSideRun(listOf(HomeKey))).isEqualTo(emptyList<HedvigNavKey>())
    assertThat(activeSideRun(listOf(HomeKey, Drill("h")))).isEqualTo(emptyList<HedvigNavKey>())
  }

  @Test fun activeSideRun_returnsSideTabKeyAndItsDrilldowns() {
    val stack = listOf(HomeKey, Drill("h"), InsurancesKey, Drill("i1"), Drill("i2"))
    assertThat(activeSideRun(stack)).isEqualTo(listOf(InsurancesKey, Drill("i1"), Drill("i2")))
  }

  @Test fun activeSideRun_sideTabRootOnly() {
    assertThat(activeSideRun(listOf(HomeKey, ProfileKey))).isEqualTo(listOf(ProfileKey))
  }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.TopLevelRunLogicTest"`
Expected: FAIL — `activeSideRun` unresolved reference.

- [ ] **Step 3: Implement the helper and remove `moveRunToTop`**

In `TopLevelRunLogic.kt`, delete the `moveRunToTop` function (lines 31-41) and add:

```kotlin
/** The active side-tab run (the side-tab key plus its drill-downs), or empty if currently on Home. */
internal fun activeSideRun(stack: List<HedvigNavKey>): List<HedvigNavKey> {
  val start = (1..stack.lastIndex).firstOrNull { stack[it].topLevelGraphOrNull() != null } ?: return emptyList()
  return stack.subList(start, stack.size).toList()
}
```

Note: `activeSideRun` and the existing `collapseToHome` partition the stack — `collapseToHome` returns `[0, start)` (the home run) and `activeSideRun` returns `[start, size)` (the side run).

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.TopLevelRunLogicTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/navigation/TopLevelRunLogic.kt app/app/src/test/kotlin/com/hedvig/android/app/navigation/TopLevelRunLogicTest.kt
git commit -m "Add activeSideRun helper, drop unused moveRunToTop"
```

---

## Phase 2 — Controller: stash/restore per-tab runs

This is the behavioral core. Switching away from a side tab stashes its run; returning restores it. Home re-selection no longer discards other tabs' runs. `currentTopLevel` stays derived; `handleBack` simplifies to a plain pop (equivalent to the old collapse-at-root logic now that only one side run is ever rendered).

### Task 2: Rewrite controller stash/restore + live-key union

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt`
- Test: `app/app/src/test/kotlin/com/hedvig/android/app/navigation/BackstackControllerTest.kt`

- [ ] **Step 1: Rewrite the controller test for the new semantics**

Replace the body of `BackstackControllerTest.kt` with the following. The constructor helper gains a parked-runs map; behavioral expectations change for tab switching (no concatenation; stash + restore).

```kotlin
package com.hedvig.android.app.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.feature.help.center.navigation.HelpCenterKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.feature.payments.navigation.PaymentsKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.core.TopLevelGraph
import org.junit.Test

internal class BackstackControllerTest {
  private fun controllerWith(vararg keys: HedvigNavKey) =
    BackstackController(mutableStateListOf(*keys), mutableStateMapOf())

  @Test
  fun `system-back at a drill-down pops one entry`() {
    val controller = controllerWith(HomeKey, HelpCenterKey)
    assertThat(controller.handleBack()).isTrue()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `system-back at a side-tab root returns to Home and parks nothing`() {
    val controller = controllerWith(HomeKey, InsurancesKey)
    assertThat(controller.handleBack()).isTrue()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
    assertThat(controller.parkedRuns).isEmpty()
  }

  @Test
  fun `system-back draining a drilled side tab drops it completely, parking nothing`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    assertThat(controller.handleBack()).isTrue() // pop Help
    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey)
    assertThat(controller.parkedRuns).isEmpty()
    assertThat(controller.handleBack()).isTrue() // pop Insurances root
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
    assertThat(controller.parkedRuns).isEmpty()
  }

  @Test
  fun `system-back never parks the active tab but leaves other parked runs intact`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Profile) // park Insurances run, render Profile root
    controller.handleBack() // drain Profile back to Home
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
    assertThat(controller.parkedRuns[TopLevelGraph.Profile]).isEqualTo(null)
    assertThat(controller.parkedRuns[TopLevelGraph.Insurances])
      .isEqualTo(listOf(InsurancesKey, HelpCenterKey))
  }

  @Test
  fun `system-back at the Home root exits the app`() {
    val controller = controllerWith(HomeKey)
    assertThat(controller.handleBack()).isFalse()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `re-tapping the current tab pops its run to the root`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Insurances)
    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey)
  }

  @Test
  fun `switching from a drilled side tab stashes its full run`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Profile)
    assertThat(controller.entries.toList()).containsExactly(HomeKey, ProfileKey)
    assertThat(controller.parkedRuns[TopLevelGraph.Insurances])
      .isEqualTo(listOf(InsurancesKey, HelpCenterKey))
  }

  @Test
  fun `returning to a stashed tab restores its whole run`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Profile) // stash Insurances run
    controller.selectTopLevel(TopLevelGraph.Insurances) // restore it
    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey, HelpCenterKey)
    assertThat(controller.parkedRuns[TopLevelGraph.Profile]).isEqualTo(listOf(ProfileKey))
    assertThat(controller.parkedRuns[TopLevelGraph.Insurances]).isEqualTo(null)
  }

  @Test
  fun `selecting Home from a side tab stashes the side run instead of discarding it`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Home)
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
    assertThat(controller.parkedRuns[TopLevelGraph.Insurances])
      .isEqualTo(listOf(InsurancesKey, HelpCenterKey))
  }

  @Test
  fun `re-selecting a side tab after going Home restores its run`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Home)
    controller.selectTopLevel(TopLevelGraph.Insurances)
    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey, HelpCenterKey)
  }

  @Test
  fun `switching to a never-visited side tab starts a fresh run`() {
    val controller = controllerWith(HomeKey)
    controller.selectTopLevel(TopLevelGraph.Payments)
    assertThat(controller.entries.toList()).containsExactly(HomeKey, PaymentsKey)
  }

  @Test
  fun `allLiveContentKeys includes parked runs`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Home) // Insurances run now parked
    assertThat(controller.allLiveContentKeys).containsExactly(
      HomeKey.toString(),
      InsurancesKey.toString(),
      HelpCenterKey.toString(),
    )
  }

  @Test
  fun `setLoggedIn pins Home and clears parked runs`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Home) // park Insurances
    controller.setLoggedIn()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
    assertThat(controller.parkedRuns).isEmpty()
    assertThat(controller.isLoggedIn).isTrue()
  }

  @Test
  fun `setLoggedOut drops to login root and clears parked runs`() {
    val controller = controllerWith(HomeKey, InsurancesKey)
    controller.selectTopLevel(TopLevelGraph.Home)
    controller.setLoggedOut()
    assertThat(controller.entries.toList()).containsExactly(LoginKey)
    assertThat(controller.parkedRuns).isEmpty()
    assertThat(controller.isLoggedIn).isFalse()
  }

  @Test
  fun `currentTopLevel tracks the nearest tab below the top`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    assertThat(controller.currentTopLevel).isEqualTo(TopLevelGraph.Insurances)
  }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.BackstackControllerTest"`
Expected: FAIL — constructor arity mismatch / `parkedRuns` and `allLiveContentKeys` unresolved.

- [ ] **Step 3: Rewrite the controller**

Replace the class in `BackstackController.kt` (keep the file's package, the existing `Backstack` import, the `replaceWith` private extension, and `rememberHedvigBackstackController` for now — `rememberHedvigBackstackController` is rewritten in Task 3). The class continues to implement `Backstack` (so `entries` is its `override`). New class body:

```kotlin
@Stable
internal class BackstackController(
  override val entries: SnapshotStateList<HedvigNavKey>,
  internal val parkedRuns: SnapshotStateMap<TopLevelGraph, List<HedvigNavKey>>,
) : Backstack {
  val isLoggedIn: Boolean
    get() = entries.firstOrNull()?.topLevelGraphOrNull() != null

  val currentTopLevel: TopLevelGraph
    get() = nearestTopLevelGraph(entries) ?: TopLevelGraph.Home

  /** The destination on top of the rendered stack — replaces Nav2's `navController.currentDestination`. */
  val currentDestination: HedvigNavKey?
    get() = entries.lastOrNull()

  /**
   * Every key whose decorator state must survive: the rendered stack plus all parked runs, mapped
   * to their `contentKey` (`toString()`). The retained decorators consult this set in `onPop` so a
   * key that merely moved into [parkedRuns] keeps its saved state and ViewModel.
   */
  val allLiveContentKeys: Set<Any>
    get() = buildSet {
      entries.forEach { add(it.toString()) }
      parkedRuns.values.forEach { run -> run.forEach { add(it.toString()) } }
    }

  /**
   * Rail/bar tap. Re-tapping the current tab pops its run to the root. Switching tabs stashes the
   * leaving side-tab's run into [parkedRuns] (Home is never parked — it stays in the rendered stack)
   * and restores the target tab's parked run, or starts a fresh one.
   */
  fun selectTopLevel(topLevelGraph: TopLevelGraph) {
    Snapshot.withMutableSnapshot {
      if (topLevelGraph == currentTopLevel) {
        entries.replaceWith(popTopRunToStart(entries))
        return@withMutableSnapshot
      }
      val leavingSideTab = nearestTopLevelGraph(entries)?.takeIf { it != TopLevelGraph.Home }
      val homeRun = collapseToHome(entries)
      if (leavingSideTab != null) {
        parkedRuns[leavingSideTab] = activeSideRun(entries)
      }
      val restored = if (topLevelGraph == TopLevelGraph.Home) {
        homeRun
      } else {
        homeRun + (parkedRuns.remove(topLevelGraph) ?: listOf(topLevelGraph.startDestination))
      }
      entries.replaceWith(restored)
    }
  }

  /**
   * System-back handler. Returns false when the app should finish. A plain pop: the rendered stack
   * is always `homeRun + at most one side run`, so popping walks up the active run, returns a side
   * root to Home, and exits from the Home root.
   *
   * It deliberately does **not** park the run it drains. Draining a side tab with system back is the
   * Nav2 "drop it completely" path: each popped key leaves the rendered stack and is absent from
   * [parkedRuns], so [allLiveContentKeys] stops covering it and the decorators dispose its saved
   * state. Only [selectTopLevel] parks a run. (Parked runs for *other* tabs are untouched here.)
   */
  fun handleBack(): Boolean {
    if (entries.size <= 1) return false
    Snapshot.withMutableSnapshot {
      entries.removeAt(entries.lastIndex)
    }
    return true
  }

  /**
   * Routes a resolved deep-link key onto the stack without creating a value-equal duplicate. Tab
   * roots go through [selectTopLevel] (which restores any parked run); any other key already present
   * is moved to the top rather than re-appended; a genuinely new key is appended.
   */
  fun navigateToDeepLink(key: HedvigNavKey) {
    val topLevelGraph = key.topLevelGraphOrNull()
    if (topLevelGraph != null) {
      selectTopLevel(topLevelGraph)
      return
    }
    Snapshot.withMutableSnapshot {
      entries.remove(key)
      entries.add(key)
    }
  }

  /** Move into the tabbed shell, Home pinned at the base; forget any parked runs. */
  fun setLoggedIn() {
    Snapshot.withMutableSnapshot {
      parkedRuns.clear()
      entries.clear()
      entries.add(HomeKey)
    }
  }

  /** Drop back to the login root; forget any parked runs. */
  fun setLoggedOut() {
    Snapshot.withMutableSnapshot {
      parkedRuns.clear()
      entries.clear()
      entries.add(LoginKey)
    }
  }
}
```

Add this import to the file (keep existing ones; `mutableStateListOf`/`rememberSerializable`/`SnapshotStateListSerializer` stay for Task 3; `TopLevelGraph` and `Backstack` are already imported):

```kotlin
import androidx.compose.runtime.snapshots.SnapshotStateMap
```

`collapseToHome`, `popTopRunToStart`, `activeSideRun`, `nearestTopLevelGraph`, and `startDestination` are already importable (same `navigation` package / existing `com.hedvig.android.app.ui.startDestination` import).

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.BackstackControllerTest"`
Expected: PASS. (The `rememberHedvigBackstackController` composable will not compile yet because its constructor call is missing the new arg — that is fixed in Task 3. If `:app` fails to compile here, temporarily expect the unit-test task to still run the test sources; if compilation blocks it, do Task 3 Step 3 first, then return to verify. Prefer completing Task 3 before running the full `:app` build.)

- [ ] **Step 5: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt app/app/src/test/kotlin/com/hedvig/android/app/navigation/BackstackControllerTest.kt
git commit -m "Stash and restore per-tab back stacks instead of discarding them"
```

---

## Phase 3 — Persist parked runs across process death

### Task 3: Serialize `parkedRuns` in `rememberHedvigBackstackController`

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt`

- [ ] **Step 1: Rewrite the composable factory**

Replace the existing `rememberHedvigBackstackController` with:

```kotlin
@Composable
internal fun rememberHedvigBackstackController(
  savedStateConfiguration: SavedStateConfiguration,
): BackstackController {
  val backstack = rememberSerializable(
    configuration = savedStateConfiguration,
    serializer = SnapshotStateListSerializer(PolymorphicSerializer(HedvigNavKey::class)),
  ) {
    mutableStateListOf<HedvigNavKey>(LoginKey)
  }
  val parkedRuns = rememberSerializable(
    configuration = savedStateConfiguration,
    serializer = SnapshotStateMapSerializer(
      TopLevelGraph.serializer(),
      ListSerializer(PolymorphicSerializer(HedvigNavKey::class)),
    ),
  ) {
    mutableStateMapOf<TopLevelGraph, List<HedvigNavKey>>()
  }
  return remember(backstack, parkedRuns) { BackstackController(backstack, parkedRuns) }
}
```

Add imports:

```kotlin
import androidx.compose.runtime.mutableStateMapOf
import androidx.savedstate.compose.serialization.serializers.SnapshotStateMapSerializer
import kotlinx.serialization.builtins.ListSerializer
```

(`SnapshotStateMapSerializer(keySerializer, valueSerializer)` is confirmed available in `androidx.savedstate:savedstate-compose`. The `SavedStateConfiguration` passed in already registers the `HedvigNavKey` polymorphic module used by `backstack`, so the same `PolymorphicSerializer(HedvigNavKey::class)` works for the map values.)

- [ ] **Step 2: Compile and run the full app unit-test suite for the package**

Run: `./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.*"`
Expected: `BUILD SUCCESSFUL`, all controller/run-logic/deep-link tests pass.

- [ ] **Step 3: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt
git commit -m "Persist parked back stacks across process death"
```

---

## Phase 4 — Union-scoped state-retention decorators

Fork Nav3's two state decorators so `onPop` disposes only when the key is absent from the live-key union. These live in `navigation-compose` `androidMain` (which already depends on `androidx.lifecycle.viewmodel.navigation3` and `androidx.navigation3.ui`). The saveable fork is built from the public `NavEntryDecorator` constructor; the ViewModel fork must copy the private `EntryViewModel` plumbing from AOSP (Apache-2.0) and change only the `onPop` predicate.

### Task 4: Forked saveable-state decorator

**Files:**
- Create: `app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/RetainedSaveableStateHolderNavEntryDecorator.kt`

- [ ] **Step 1: Write the decorator**

```kotlin
package com.hedvig.android.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.navigation3.runtime.NavEntryDecorator
import com.hedvig.android.navigation.common.HedvigNavKey

/**
 * Drop-in replacement for `rememberSaveableStateHolderNavEntryDecorator` that retains the saved
 * state of entries which leave the rendered back stack but remain "live" (parked in another tab).
 *
 * The stock decorator removes an entry's `rememberSaveable`/`SavedStateHandle` state the moment the
 * entry leaves the rendered back stack. We instead consult [retainedContentKeys] (the union of the
 * rendered stack and all parked tab runs) and only remove state for keys that are genuinely gone —
 * popped to nowhere, not merely parked.
 */
@Composable
internal fun rememberRetainedSaveableStateHolderNavEntryDecorator(
  retainedContentKeys: () -> Set<Any>,
  saveableStateHolder: SaveableStateHolder = rememberSaveableStateHolder(),
): NavEntryDecorator<HedvigNavKey> {
  val latestRetained by rememberUpdatedState(retainedContentKeys)
  return remember(saveableStateHolder) {
    NavEntryDecorator(
      onPop = { contentKey ->
        if (contentKey !in latestRetained()) {
          saveableStateHolder.removeState(contentKey)
        }
      },
      decorate = { entry ->
        saveableStateHolder.SaveableStateProvider(entry.contentKey) { entry.Content() }
      },
    )
  }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew :navigation-compose:compileDebugKotlinAndroid`
Expected: `BUILD SUCCESSFUL`. (Resolve the exact task name via `./gradlew :navigation-compose:tasks | grep -i compile` if needed; the module is `app/navigation/navigation-compose`.)

- [ ] **Step 3: Commit**

```bash
git add app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/RetainedSaveableStateHolderNavEntryDecorator.kt
git commit -m "Add union-scoped saveable-state NavEntry decorator"
```

### Task 5: Forked ViewModelStore decorator

**Files:**
- Create: `app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/RetainedViewModelStoreNavEntryDecorator.kt`

- [ ] **Step 1: Write the decorator (copied from AOSP, `onPop` predicate changed)**

This reproduces `androidx.lifecycle.viewmodel.navigation3.ViewModelStoreNavEntryDecorator`'s `decorate` block verbatim (the `SavedStateRegistryOwner` wiring is load-bearing for `SavedStateHandle` support) and replaces its `removeViewModelStoreOnPop` gate with a per-key union check.

```kotlin
package com.hedvig.android.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import com.hedvig.android.navigation.common.HedvigNavKey

/**
 * Drop-in replacement for `rememberViewModelStoreNavEntryDecorator` that retains the ViewModelStore
 * of entries which leave the rendered back stack but remain "live" (parked in another tab).
 *
 * The stock decorator clears an entry's ViewModelStore when it is popped from the rendered stack.
 * We instead consult [retainedContentKeys] (the union of the rendered stack and all parked tab runs)
 * and clear only the stores of keys that are genuinely gone. A key that merely moved into a parked
 * run keeps its ViewModels (and their SavedStateHandles) alive for when the run is restored.
 */
@Composable
internal fun rememberRetainedViewModelStoreNavEntryDecorator(
  retainedContentKeys: () -> Set<Any>,
  viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
  },
): NavEntryDecorator<HedvigNavKey> {
  val latestRetained by rememberUpdatedState(retainedContentKeys)
  return remember(viewModelStoreOwner) {
    RetainedViewModelStoreNavEntryDecorator(viewModelStoreOwner.viewModelStore) { contentKey ->
      contentKey !in latestRetained()
    }
  }
}

private class RetainedViewModelStoreNavEntryDecorator(
  rootViewModelStore: ViewModelStore,
  shouldRemoveStoreForKey: (Any) -> Boolean,
) : NavEntryDecorator<HedvigNavKey>(
    onPop = { contentKey ->
      if (shouldRemoveStoreForKey(contentKey)) {
        rootViewModelStore.getEntryViewModel().clearViewModelStoreOwnerForKey(contentKey)
      }
    },
    decorate = { entry ->
      val entryViewModelStore = rootViewModelStore.getEntryViewModel().viewModelStoreForKey(entry.contentKey)
      val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
      val childViewModelStoreOwner = remember {
        object :
          ViewModelStoreOwner,
          SavedStateRegistryOwner by savedStateRegistryOwner,
          HasDefaultViewModelProviderFactory {
          override val viewModelStore: ViewModelStore
            get() = entryViewModelStore

          override val defaultViewModelProviderFactory: ViewModelProvider.Factory
            get() = SavedStateViewModelFactory()

          override val defaultViewModelCreationExtras: CreationExtras
            get() = MutableCreationExtras().also {
              it[SAVED_STATE_REGISTRY_OWNER_KEY] = this
              it[VIEW_MODEL_STORE_OWNER_KEY] = this
            }

          init {
            require(this.lifecycle.currentState == Lifecycle.State.INITIALIZED) {
              "The Lifecycle state is already beyond INITIALIZED. The " +
                "RetainedViewModelStoreNavEntryDecorator requires adding the " +
                "saveable-state decorator to ensure support for SavedStateHandles."
            }
            enableSavedStateHandles()
          }
        }
      }
      CompositionLocalProvider(LocalViewModelStoreOwner provides childViewModelStoreOwner) {
        entry.Content()
      }
    },
  )

private class EntryViewModel : ViewModel() {
  private val owners = mutableMapOf<Any, ViewModelStore>()

  fun viewModelStoreForKey(key: Any): ViewModelStore = owners.getOrPut(key) { ViewModelStore() }

  fun clearViewModelStoreOwnerForKey(key: Any) {
    owners.remove(key)?.clear()
  }

  override fun onCleared() {
    owners.forEach { (_, store) -> store.clear() }
  }
}

private fun ViewModelStore.getEntryViewModel(): EntryViewModel {
  val provider = ViewModelProvider.create(
    store = this,
    factory = viewModelFactory { initializer { EntryViewModel() } },
  )
  return provider[EntryViewModel::class]
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew :navigation-compose:compileDebugKotlinAndroid`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/RetainedViewModelStoreNavEntryDecorator.kt
git commit -m "Add union-scoped ViewModelStore NavEntry decorator"
```

---

## Phase 5 — Wire the decorators into the display

### Task 6: `HedvigNavDisplay` uses the retained decorators

**Files:**
- Modify: `app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/HedvigNavDisplay.kt`

- [ ] **Step 1: Add the live-key parameter and swap the decorator list**

Add a parameter `retainedContentKeys: () -> Set<Any>` to `HedvigNavDisplay` (place it right after `onBack`), and replace the `entryDecorators` list. Remove the now-unused imports `androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator` and `androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator`.

New signature fragment and body change:

```kotlin
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HedvigNavDisplay(
  backstack: Backstack,
  onBack: () -> Unit,
  retainedContentKeys: () -> Set<Any>,
  enterTransition: EnterTransition,
  exitTransition: ExitTransition,
  popEnterTransition: EnterTransition,
  popExitTransition: ExitTransition,
  modifier: Modifier = Modifier,
  sharedTransitionScope: SharedTransitionScope? = null,
  sceneDecoratorStrategies: List<SceneDecoratorStrategy<HedvigNavKey>> = emptyList(),
  builder: EntryProviderScope<HedvigNavKey>.() -> Unit,
) {
  NavDisplay(
    backstack = backstack.entries,
    modifier = modifier,
    onBack = { onBack() },
    entryDecorators = listOf(
      rememberRetainedSaveableStateHolderNavEntryDecorator(retainedContentKeys),
      rememberRetainedViewModelStoreNavEntryDecorator(retainedContentKeys),
    ),
    sharedTransitionScope = sharedTransitionScope,
    sceneDecoratorStrategies = sceneDecoratorStrategies,
    transitionSpec = { enterTransition togetherWith exitTransition },
    popTransitionSpec = { popEnterTransition togetherWith popExitTransition },
    predictivePopTransitionSpec = { popEnterTransition togetherWith popExitTransition },
    entryProvider = entryProvider(builder = builder),
  )
}
```

Decorator order is preserved (saveable outer, ViewModel inner) — the ViewModel decorator requires the saveable one above it for `SavedStateHandle` support.

- [ ] **Step 2: Compile**

Run: `./gradlew :navigation-compose:compileDebugKotlinAndroid`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/HedvigNavDisplay.kt
git commit -m "Wire retained decorators into HedvigNavDisplay"
```

### Task 7: `HedvigNavHost` passes the live-key union

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt:130-143`

- [ ] **Step 1: Pass `retainedContentKeys`**

In the `HedvigNavDisplay(...)` call, add the argument right after `onBack`:

```kotlin
  HedvigNavDisplay(
    backstack = hedvigAppState.backstackController,
    onBack = {
      if (!hedvigAppState.backstackController.handleBack()) {
        finishApp()
      }
    },
    retainedContentKeys = { hedvigAppState.backstackController.allLiveContentKeys },
    enterTransition = MotionDefaults.sharedXAxisEnter(density),
    // ...rest unchanged...
```

- [ ] **Step 2: Compile the app**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt
git commit -m "Feed live-key union to the nav display for per-tab state retention"
```

---

## Phase 6 — Verify

### Task 8: Full automated checks

**Files:** none (verification only)

- [ ] **Step 1: Run the navigation unit tests**

Run: `./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.*"`
Expected: `BUILD SUCCESSFUL` — all controller, run-logic, and deep-link tests pass.

- [ ] **Step 2: ktlint**

Run: `./gradlew :app:ktlintCheck :navigation-compose:ktlintCheck :navigation-core:ktlintCheck`
Expected: `BUILD SUCCESSFUL`. If it fails, run the matching `ktlintFormat` tasks and re-check.

- [ ] **Step 3: Assemble the app**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. (Grep the output for an explicit `BUILD SUCCESSFUL` — a piped `gradlew | tail` can mask a `BUILD FAILED` exit code.)

### Task 9: Manual instrumented verification (keys + saved state + predictive back)

The decorators are exercisable only against the live Compose/ViewModel runtime, so confirm them by hand on a device/emulator. Run a debug build and walk each scenario, watching for regressions.

- [ ] **Keys remembered across Home:** Insurances tab → drill down two screens. Tap Home (system-back from Home root should still exit — verify last). Tap Insurances → the two drilled screens are restored (you land on the deepest screen, back walks up the run).
- [ ] **Saved state retained:** In a drilled Insurances screen, scroll partway / enter text in a field. Switch to Profile, then back to Insurances → scroll position / field contents are preserved (not reset). This proves the forked decorators retain `rememberSaveable` + `SavedStateHandle`/ViewModel state, not just keys.
- [ ] **Independent per-tab runs:** Drill into Insurances, switch to Payments and drill there, switch back to Insurances → Insurances run intact; switch to Payments → Payments run intact.
- [ ] **Predictive back is correct everywhere:** Slow back-swipe at an Insurances drill-down previews its parent; at the Insurances root previews Home; at the Home root previews exit/launcher. No snap between the previewed and committed destination.
- [ ] **Back from Home exits:** From Home root, system back finishes the app (does not wander into a parked tab).
- [ ] **Re-tap resets:** Drill into a tab, re-tap that tab's rail/bar item → pops to the tab root (drilled state intentionally forgotten).
- [ ] **System-back drops completely (Nav2 parity):** Drill into Insurances two screens, then drain the whole run with *system back* (not a tab tap) until you reach Home. Re-tap Insurances → you land on a **fresh** Insurances root with no drilled screens and no retained scroll/field state. Contrast with the "Keys remembered" scenario above: only leaving via a tab tap preserves the run; leaving via system back discards it.
- [ ] **Process death:** Drill into Insurances, park it by going Home, then trigger "Don't keep activities" (or `adb shell am kill`) and relaunch → parked Insurances run and current Home position are restored.
- [ ] **Login flow unaffected:** Log out → multi-screen login flow back-navigates per screen and exits from the login root; log in → lands on Home with no parked runs.

- [ ] **Step (final): Commit any formatting-only changes** produced by `ktlintFormat`, if any.

```bash
git add -A
git commit -m "Format per-tab back-stack changes"
```

---

## Self-Review notes

- **Spec coverage:** keys remembered (Phase 2 stash/restore), saved state remembered (Phases 4-5 union-scoped decorators), predictive back preserved (flat-stack invariant + Background §2), back-from-Home exits (handleBack plain pop + invariant), login multi-screen flow (already in the flat stack; `setLoggedIn/Out` clear parked runs), process-death persistence (Phase 3). All covered.
- **Type consistency:** `allLiveContentKeys: Set<Any>` (controller) ↔ `retainedContentKeys: () -> Set<Any>` (display/decorators); `contentKey` compared as `String` (`key.toString()`) on both sides; `parkedRuns: SnapshotStateMap<TopLevelGraph, List<HedvigNavKey>>` consistent across controller, factory, and serializer.
- **Open risk to validate during implementation:** the exact Gradle module path/task names for `navigation-compose` and `navigation-core` (use `./gradlew projects` to confirm `:navigation-compose` vs `:navigation:navigation-compose`). The decorator retention itself has no unit test (requires the Compose runtime); it is covered by the Task 9 manual checklist — flag to the reviewer if automated coverage is required, in which case add a Robolectric `runComposeUiTest` test in `navigation-compose` (the catalog already has `robolectric` and `ui-test-manifest`).
```
