# Nav3 "Key used multiple times" Crash Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eliminate the `IllegalArgumentException: Key <X> was used multiple times` crash (Crashlytics `f26e8107ace4f9542b53486a6a1a42af`, b/516312097) by making our custom Nav3 `Scene`s stable across recomposition and by stopping `authStatus` from reporting a transient `LoggedIn` for an unrecoverable (expired-refresh) session.

**Architecture:** The crash fires in `SaveableStateProvider` when two live entries share a `contentKey`. Two independent contributors: (1) our custom `Scene`s compare `NavEntry` by value, which drags in `NavEntry.content ===` identity and makes a logically-identical scene look "new" during a transition; (2) `AuthTokenServiceImpl.authStatus` emits `LoggedIn` for a stored-but-expired session, which the reconciler turns into a rapid `Login -> Home -> Login` re-root mid scene-transition. We fix both at the source, add a comment/tracking trail to b/516312097, and bump `navigation3` to pick up the framework-side fix.

**Tech Stack:** Kotlin, Jetpack Compose, Navigation 3 (`androidx.navigation3`), Metro DI, kotlinx.coroutines, JUnit4 + assertk + Turbine, kotlin.time.Clock.

## Global Constraints

- Never use the em-dash / spaced-dash " - " in prose or code comments. Use a comma, period, parentheses, or colon. (User global rule.)
- Run `./gradlew ktlintFormat` before every commit. Max line length 120, 2-space indent, trailing commas on.
- DI is Metro. No Koin, no `module {}`, no `get()`.
- Do not add strings to any `strings.xml` (Lokalise-managed).
- Gradle output can mask failures when piped: confirm an explicit `BUILD SUCCESSFUL` line, do not trust a `tail`/`grep` exit code.
- Current `androidx-navigation3` version floor: `1.2.0-alpha03` (`gradle/libs.versions.toml:46`).
- Gradle module paths: `:app` (`app/app`), `:auth-core-public` (`app/auth/auth-core-public`), `:navigation-compose` (`app/navigation/navigation-compose`).
- Branch off `develop` before starting: `git switch -c fix/nav3-duplicate-key-crash`.

## Decisions locked (from planning)

- **Refresh-token expiry:** exact expiry, no safety buffer. `authStatus` is `LoggedOut` only once the refresh token is strictly at/after `expiryDate`.
- **Nav3 bump:** yes, bump to the latest `1.2.0-alphaNN` in parallel (Task 5), keeping the app-side scene fixes regardless.
- **Test depth:** unit tests only. No instrumented/emulator repro.

## Open decision (needs answer before Task 2's test step)

- **D1 - testing the two decorator scenes (`NavSuiteScene`, `NavUpBarScene`):** their equality reduces to `key` (`scene::class to scene.key`, already a stable value), but constructing one in a test needs a Compose `SharedTransitionScope`, and the repo has no Robolectric-compose test infra today. Recommended: **do not** add a dedicated test for these two; the identical `NavEntry`-instability principle is proven by Task 1's `BottomSheetScene` test and the `key`-only equality is stable by construction. Alternative: add net-new Robolectric-compose infra and a `decorateScene` equality test. Task 2 is written for the recommended path; switch only if you pick the alternative.

---

### Task 1: Stabilize `BottomSheetScene` equality

**Files:**
- Modify: `app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/BottomSheetSceneStrategy.kt:71-82`
- Test: `app/app/src/test/kotlin/com/hedvig/android/app/navigation/BottomSheetSceneStrategyTest.kt`

**Interfaces:**
- Consumes: `BottomSheetSceneStrategy.calculateScene(entries)` (existing), `BottomSheetSceneStrategy.bottomSheet()` metadata (existing).
- Produces: no signature change. `BottomSheetScene.equals`/`hashCode` now compare by `contentKey` only.

- [ ] **Step 1: Write the failing test**

Add to `BottomSheetSceneStrategyTest.kt` (the `entry(...)` helper already creates a fresh `content` lambda per call, so two calls with the same key differ only by lambda identity):

```kotlin
@Test
fun `bottom-sheet scenes for the same content keys are equal despite distinct content lambdas`() {
  fun buildScene() = with(strategy()) {
    with(SceneStrategyScope<String>()) {
      calculateScene(listOf(entry("a"), entry("b", BottomSheetSceneStrategy.bottomSheet())))
    }
  }
  val first = buildScene()!!
  val second = buildScene()!!
  assertThat(first == second).isTrue()
  assertThat(first.hashCode() == second.hashCode()).isTrue()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.BottomSheetSceneStrategyTest"`
Expected: FAIL on the new test (`first == second` is false, because the current `equals` compares `entry`/`previousEntries` by `NavEntry` identity).

- [ ] **Step 3: Replace `equals`/`hashCode` to compare by `contentKey`**

In `BottomSheetSceneStrategy.kt`, replace the body of `equals`/`hashCode` (`:71-82`):

```kotlin
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BottomSheetScene<*>) return false
    return key == other.key &&
      previousEntries.map { it.contentKey } == other.previousEntries.map { it.contentKey } &&
      entry.contentKey == other.entry.contentKey
  }

  override fun hashCode(): Int {
    var result = key.hashCode()
    result = 31 * result + previousEntries.map { it.contentKey }.hashCode()
    result = 31 * result + entry.contentKey.hashCode()
    return result
  }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.BottomSheetSceneStrategyTest"`
Expected: PASS (all tests, including the two pre-existing ones).

- [ ] **Step 5: Format and commit**

```bash
./gradlew ktlintFormat
git add app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/BottomSheetSceneStrategy.kt \
        app/app/src/test/kotlin/com/hedvig/android/app/navigation/BottomSheetSceneStrategyTest.kt
git commit -m "Fix BottomSheetScene equality to compare by contentKey (b/516312097)"
```

---

### Task 2: Stabilize `NavSuiteScene` and `NavUpBarScene` equality

**Files:**
- Modify: `app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/NavSuiteSceneDecorator.kt:150-156` (`NavSuiteScene`) and `:207-211` (`NavUpBarScene`)

**Interfaces:**
- Consumes: `Scene<T>.key` of the wrapped scene (`SinglePaneScene.key` is the entry `contentKey`, a stable value).
- Produces: no signature change. Both scenes now define explicit `equals`/`hashCode` keyed on their own `key` (`scene::class to scene.key`), instead of the `data class` default that delegated to the wrapped scene's `NavEntry`-identity comparison.

Note: no code destructures or `copy()`s these classes (verified), so dropping `data class` is safe.

- [ ] **Step 1: Convert `NavSuiteScene` from `data class` to `class` and add explicit equality**

In `NavSuiteSceneDecorator.kt`, change the declaration at `:151` from `private data class NavSuiteScene<T : Any>(` to `private class NavSuiteScene<T : Any>(` and add, inside the class body (after `onRemove`/`content`, before the closing brace at `:205`):

```kotlin
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is NavSuiteScene<*>) return false
    return key == other.key
  }

  override fun hashCode(): Int = key.hashCode()
```

- [ ] **Step 2: Convert `NavUpBarScene` from `data class` to `class` and add explicit equality**

Change the declaration at `:207` from `private data class NavUpBarScene<T : Any>(` to `private class NavUpBarScene<T : Any>(` and add, inside the class body (before the closing brace at `:226`):

```kotlin
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is NavUpBarScene<*>) return false
    return key == other.key
  }

  override fun hashCode(): Int = key.hashCode()
```

- [ ] **Step 3: Compile the module to verify no `data class` API was relied on**

Run: `./gradlew :navigation-compose:compileDebugKotlin` (or `:navigation-compose:assemble` if the Android compile task name differs)
Expected: `BUILD SUCCESSFUL`, no "unresolved reference: copy/component1" errors.

- [ ] **Step 4: (Per decision D1) No dedicated unit test**

Leave the code comment added in Task 4 as the documentation trail. The `NavEntry`-instability principle is covered by Task 1's `BottomSheetScene` test, and `key` (`scene::class to scene.key`) is a stable value by construction. If you instead chose the alternative in D1, stop and add the Robolectric-compose test before continuing.

- [ ] **Step 5: Format and commit**

```bash
./gradlew ktlintFormat
git add app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/NavSuiteSceneDecorator.kt
git commit -m "Stabilize NavSuiteScene/NavUpBarScene equality by contentKey (b/516312097)"
```

---

### Task 3: Make `authStatus` treat an expired-refresh session as logged out

**Files:**
- Modify: `app/auth/auth-core-public/src/main/kotlin/com/hedvig/android/auth/AuthTokenServiceImpl.kt:28-43`
- Test: `app/auth/auth-core-public/src/test/kotlin/com/hedvig/android/auth/AuthTokenServiceImplTest.kt` (create)

**Interfaces:**
- Consumes: `AuthTokenStorage.getTokens(): Flow<AuthTokens?>`, `AuthTokens.refreshToken.expiryDate: Instant`, `kotlin.time.Clock`.
- Produces: `AuthTokenServiceImpl(authTokenStorage, authRepository, authEventStorage, coroutineScope, clock: Clock = Clock.System)` (new trailing `clock` param, defaulted). `authStatus` emits `LoggedOut` when the stored refresh token's `expiryDate <= clock.now()`.

- [ ] **Step 1: Write the failing test**

Create `AuthTokenServiceImplTest.kt`. It builds a real `AuthTokenStorage` over an in-memory datastore (mirroring `AndroidAccessTokenProviderTest`), seeds a session, advances a `TestClock` past the refresh expiry, then constructs the service so the eager `stateIn` maps at the advanced time:

```kotlin
package com.hedvig.android.auth

import app.cash.turbine.Turbine
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.auth.event.AuthEventStorage
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.auth.test.FakeAuthRepository
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.test.clock.TestClock
import com.hedvig.android.test.datastore.TestPreferencesDataStore
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class AuthTokenServiceImplTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @Test
  fun `authStatus is LoggedOut when the stored refresh token is expired`() = runTest {
    val clock = TestClock()
    val storage = authTokenStorage(clock)
    storage.updateTokens(
      accessToken = AccessToken("access", expiryInSeconds = 60),
      refreshToken = RefreshToken("refresh", expiryInSeconds = 120),
    )
    clock.advanceTimeBy(121.seconds)
    val service = authTokenService(storage, clock)

    val status = service.authStatus.filterNotNull().first()

    assertThat(status).isEqualTo(AuthStatus.LoggedOut)
  }

  @Test
  fun `authStatus is LoggedIn when the stored refresh token is still valid`() = runTest {
    val clock = TestClock()
    val storage = authTokenStorage(clock)
    storage.updateTokens(
      accessToken = AccessToken("access", expiryInSeconds = 60),
      refreshToken = RefreshToken("refresh", expiryInSeconds = 120),
    )
    clock.advanceTimeBy(30.seconds)
    val service = authTokenService(storage, clock)

    val status = service.authStatus.filterNotNull().first()

    assertThat(status).isInstanceOf(AuthStatus.LoggedIn::class)
  }

  private fun TestScope.authTokenService(
    storage: AuthTokenStorage,
    clock: Clock,
  ): AuthTokenService = AuthTokenServiceImpl(
    storage,
    FakeAuthRepository(),
    AuthEventStorage(),
    ApplicationScope(backgroundScope),
    clock,
  )

  private fun TestScope.authTokenStorage(clock: Clock) = AuthTokenStorage(
    TestPreferencesDataStore(
      datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
      coroutineScope = backgroundScope,
    ),
    clock,
  )
}
```

Note: confirm the exact `AccessToken`/`RefreshToken` constructor argument name (`expiryInSeconds`) against `com.hedvig.authlib` and the `TestPreferencesDataStore` import path used by `AndroidAccessTokenProviderTest` (copy them verbatim from that file if they differ).

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :auth-core-public:test --tests "com.hedvig.android.auth.AuthTokenServiceImplTest"`
Expected: FAIL on the expired case (`authStatus` currently returns `LoggedIn` because it only checks token presence).

- [ ] **Step 3: Add the clock dependency and gate `LoggedIn` on refresh expiry**

In `AuthTokenServiceImpl.kt`, add the import `import kotlin.time.Clock`, add the constructor param, and rewrite the `authStatus` mapping:

```kotlin
internal class AuthTokenServiceImpl(
  private val authTokenStorage: AuthTokenStorage,
  private val authRepository: AuthRepository,
  private val authEventStorage: AuthEventStorage,
  coroutineScope: ApplicationScope,
  private val clock: Clock = Clock.System,
) : AuthTokenService {
  override val authStatus: StateFlow<AuthStatus?> = authTokenStorage.getTokens()
    .mapLatest { authTokens ->
      val tokens = authTokens ?: return@mapLatest AuthStatus.LoggedOut
      // A stored session whose refresh token has already expired is unrecoverable. There is no valid
      // grant left to exchange, so the next authenticated request would fail its refresh and clear the
      // tokens anyway. Reporting LoggedIn here roots the app in the logged-in scene for a few frames,
      // then the forced logout flips it back to Login, a rapid re-root that crashes Nav3's scene
      // SaveableStateHolder ("Key <X> was used multiple times", b/516312097). Treat it as logged out up
      // front. Expiry is evaluated whenever storage emits; an in-session expiry is still handled by the
      // request-path refresh failure, which clears tokens and re-triggers this mapping.
      if (tokens.refreshToken.expiryDate <= clock.now()) {
        return@mapLatest AuthStatus.LoggedOut
      }
      AuthStatus.LoggedIn(tokens.accessToken, tokens.refreshToken)
    }
    .stateIn(
      coroutineScope,
      SharingStarted.Eagerly,
      null,
    )
```

- [ ] **Step 4: Update the sibling test factory so it still compiles**

`AndroidAccessTokenProviderTest.authTokenService(...)` constructs `AuthTokenServiceImpl(...)` positionally (`:188-193`). The new `clock` param is defaulted, so that call still compiles unchanged. No edit required. (If a compile error appears, add `Clock.System` as the trailing argument there.)

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :auth-core-public:test`
Expected: `BUILD SUCCESSFUL`, `AuthTokenServiceImplTest` passes and `AndroidAccessTokenProviderTest` still passes.

- [ ] **Step 6: Format and commit**

```bash
./gradlew ktlintFormat
git add app/auth/auth-core-public/src/main/kotlin/com/hedvig/android/auth/AuthTokenServiceImpl.kt \
        app/auth/auth-core-public/src/test/kotlin/com/hedvig/android/auth/AuthTokenServiceImplTest.kt
git commit -m "Treat expired-refresh session as logged out to stop start-scene flap (b/516312097)"
```

---

### Task 4: Document the b/516312097 trail in code

**Files:**
- Modify: `app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/RetainedSaveableStateHolderNavEntryDecorator.kt:64-66` (the `SaveableStateProvider` call site, the crash line)

**Interfaces:** none. Comment only.

- [ ] **Step 1: Add a comment at the crash site**

Immediately above the `decorate = { entry -> ... saveableStateHolder.SaveableStateProvider(entry.contentKey) ... }` block, add:

```kotlin
      // NavDisplay must never hand two live entries the same contentKey here, or SaveableStateProvider
      // throws "Key <X> was used multiple times" (b/516312097). Our custom Scenes therefore compare by
      // contentKey, not NavEntry identity (see BottomSheetScene and NavSuiteScene/NavUpBarScene), and
      // AuthTokenServiceImpl avoids the transient logged-in start-scene flap that stressed this path.
```

- [ ] **Step 2: Format, compile, commit**

```bash
./gradlew ktlintFormat
./gradlew :navigation-compose:compileDebugKotlin
git add app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/RetainedSaveableStateHolderNavEntryDecorator.kt
git commit -m "Document b/516312097 duplicate-key invariant at the SaveableStateProvider call site"
```

---

### Task 5: Bump `navigation3` to the latest 1.2.0 alpha

**Files:**
- Modify: `gradle/libs.versions.toml:46` (`androidx-navigation3`) and, if needed for compatibility, `:47` (`androidx-lifecycle-viewmodel-navigation3`)

**Interfaces:** none. Version bump. Keep all Task 1-3 changes regardless of the framework fix.

- [ ] **Step 1: Find the latest published version**

Check Google Maven for the newest `androidx.navigation3:navigation3-ui` under `1.2.0-alphaNN` (NN > 3). Record the version. Confirm whether `androidx.lifecycle:lifecycle-viewmodel-navigation3` (currently `2.10.0`) needs a matching bump per the release notes.

- [ ] **Step 2: Update the version catalog**

Set `androidx-navigation3 = "1.2.0-alpha0N"` in `gradle/libs.versions.toml:46` (use the version from Step 1). Update `androidx-lifecycle-viewmodel-navigation3` only if Step 1 showed it is required.

- [ ] **Step 3: Build the app and run the nav + auth tests**

Run: `./gradlew :app:assembleDebug :app:testDebugUnitTest :navigation-compose:test :auth-core-public:test`
Expected: `BUILD SUCCESSFUL`. If Nav3 changed public API (scene/decorator strategy signatures, `NavDisplay` params), fix call sites in `HedvigApp.kt`, `NavSuiteSceneDecorator.kt`, `BottomSheetSceneStrategy.kt`, and the entry-decorator files, then re-run.

- [ ] **Step 4: Run the back-stack serialization guards**

Run: `./gradlew :app:testDebugUnitTest --tests "*ExhaustiveBackStackSerializationTest" --tests "*BackstackTest"`
Expected: PASS (no regression in key serialization / back-stack behavior).

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "Bump navigation3 to 1.2.0-alpha0N for the OverlayScene duplicate-key fix (b/516312097)"
```

---

## Self-Review

**Spec coverage:**
- Scene-equals stability for all custom scenes: Task 1 (`BottomSheetScene`), Task 2 (`NavSuiteScene`, `NavUpBarScene`). Framework `SinglePaneScene` is not ours: covered by Task 5's bump.
- `authStatus` expiry flap: Task 3.
- Tracking / documentation trail: Task 4 (code) plus a `reference` memory to write out-of-band linking b/516312097 to our `NavSuiteScene`/`BottomSheetScene` exposure.
- Framework fix: Task 5.

**Placeholder scan:** two verification points are intentionally left to the implementer because they depend on external facts, not design choices: the exact `com.hedvig.authlib` token constructor arg name / `TestPreferencesDataStore` import in Task 3 Step 1 (copy verbatim from `AndroidAccessTokenProviderTest`), and the latest `navigation3` version in Task 5 Step 1.

**Type consistency:** `AuthTokenServiceImpl` gains a trailing `clock: Clock = Clock.System` used identically in the impl and both test factories. Scene `equals`/`hashCode` compare `contentKey`/`key` consistently across Tasks 1 and 2.

## Manual verification (no automated repro, per decision)

After all tasks: install a debug build, sign in, then force the reported condition by letting the stored refresh token expire (or shorten it) and cold-starting. Confirm the app lands on Login without crashing and that `authStatus` logs `LoggedOut` without a `Navigated to HomeKey` -> `Navigated to LoginKey` flap in Logcat.
