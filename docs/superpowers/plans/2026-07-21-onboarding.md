# Onboarding Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A post-login onboarding wizard shown once per member, with a step path computed on-device from one eagerly-fetched GraphQL query, plus the new tri-state Firebase analytics consent mechanism.

**Architecture:** New `feature-onboarding` module with tiny per-step nav keys over a shared `ActivityRetainedScope` session store (spec's "approach C"). A gate effect in `HedvigApp` pushes the flow on top of Home for unseen members. Consent gating wraps the Firebase tracking client with a buffer-and-flush delegate; Datadog is untouched.

**Tech Stack:** Kotlin, Jetpack Compose, Molecule (MVI), Apollo GraphQL (octopus), Metro DI, Navigation 3, DataStore Preferences, Turbine/assertK tests.

**Spec:** `docs/superpowers/specs/2026-07-21-onboarding-design.md`

## Global Constraints

- Never use " — " (spaced em-dash) in any prose, comment, or commit message; use comma/period/parens/colon.
- Never add strings to any `strings.xml`. Hardcode English text with a `// TODO: Add "<English>" / "<Swedish>" to Lokalise` comment.
- Apollo-generated `octopus.*` types must never appear in public signatures; confine them to `internal` repository impls.
- All logging through `com.hedvig.android.logger.logcat` (`projects.loggingPublic`); never Timber/Log/println.
- Feature modules cannot depend on feature modules, EXCEPT `feature-{name}-navigation` modules. `ChipIdKey` lives in `feature-chip-id` (not a `-navigation` module), so onboarding must NOT depend on it; thread a lambda from `:app` instead.
- `backstack.navigateUp()` only for the top-app-bar back arrow; every other pop uses `popBackstack()` / removal extensions.
- Run `./gradlew ktlintFormat` before every commit. 2-space indent, max line 120, trailing commas.
- Gradle verification: do NOT trust piped exit codes (`gradlew ... | tail` returns tail's exit code). Grep the output for `BUILD SUCCESSFUL` explicitly.
- Every new `@Serializable` `HedvigNavKey` requires `navKeys()` in the module's `hedvig {}` block and `@androidx.annotation.Keep` on the key class (see `ChipIdNavDestination.kt` precedent).
- Metro: KSP-generated ViewModel contributions are `public` by design; don't "fix" them to internal.
- Commit after every task (small, atomic commits). Branch: `feat/onboarding`.

## File Structure Overview

New module `app/feature/feature-onboarding/` (auto-discovered by settings.gradle.kts):

```
app/feature/feature-onboarding/
├── build.gradle.kts
└── src/main/
    ├── graphql/
    │   ├── OnboardingQuery.graphql
    │   └── UpdateContactInfoMutation.graphql
    └── kotlin/com/hedvig/android/feature/onboarding/
        ├── navigation/
        │   ├── OnboardingKeys.kt            (OnboardingKey, OnboardingStepKey, OnboardingStepId)
        │   ├── OnboardingEntries.kt          (entries function, filled in Task 19)
        │   └── OnboardingNavigator.kt        (ActivityRetainedScope, continue/exit/back)
        ├── data/
        │   ├── OnboardingData.kt             (project-owned models)
        │   ├── OnboardingRepository.kt       (interface + internal Apollo impl)
        │   ├── OnboardingPath.kt             (buildOnboardingPath pure function)
        │   ├── OnboardingSeenStore.kt        (per-member DataStore flag)
        │   ├── OnboardingSessionStore.kt     (ActivityRetainedScope cache of data+path)
        │   └── ShouldShowOnboardingUseCase.kt
        ├── gate/
        │   └── OnboardingGate.kt             (public interface + impl, ActivityRetainedScope)
        └── ui/
            ├── OnboardingStepScaffold.kt     (progress bar + back + close chrome)
            ├── welcome/WelcomeDestination.kt
            ├── consent/ConsentStepDestination.kt        (+ ViewModel/Presenter)
            ├── phone/PhoneStepDestination.kt            (+ ViewModel/Presenter)
            ├── theme/ThemeStepDestination.kt            (+ ViewModel/Presenter)
            ├── coinsured/CoInsuredStepDestination.kt    (+ ViewModel/Presenter)
            ├── petid/PetIdStepDestination.kt            (+ ViewModel/Presenter)
            ├── invite/InviteStepDestination.kt          (+ ViewModel/Presenter)
            ├── payment/PaymentStepDestination.kt        (+ ViewModel/Presenter)
            └── bundle/BundleStepDestination.kt          (+ ViewModel/Presenter)
```

Modified existing files:

- `app/data/data-settings-datastore/data-settings-datastore-public/.../SettingsDataStoreImpl.kt` (tri-state consent)
- `app/tracking/tracking-firebase/.../FirebaseEventTrackingClient.kt` (lose the binding annotation)
- `app/tracking/tracking-firebase/.../ConsentAwareEventTrackingClient.kt` (new, becomes the binding)
- `app/tracking/tracking-firebase/build.gradle.kts` (add settings-datastore dep)
- `app/feature/feature-profile/.../settings/SettingsPresenter.kt` + `SettingsDestination.kt` (consent toggle row)
- `app/app/build.gradle.kts` (add feature-onboarding dep)
- `app/app/.../navigation/HedvigEntryProvider.kt` (register onboardingEntries)
- `app/app/.../di/ActivityRetainedGraph.kt` (expose OnboardingGate)
- `app/app/.../ui/HedvigApp.kt` (TryShowOnboardingEffect)
- `app/app/.../MainActivity.kt` (pass gate into HedvigApp)

---

### Task 1: Tri-state AnalyticsConsent in SettingsDataStore

**Files:**
- Create: `app/data/data-settings-datastore/data-settings-datastore-public/src/main/kotlin/com/hedvig/android/data/settings/datastore/AnalyticsConsent.kt`
- Modify: `app/data/data-settings-datastore/data-settings-datastore-public/src/main/kotlin/com/hedvig/android/data/settings/datastore/SettingsDataStoreImpl.kt`
- Modify: `app/data/data-settings-datastore/data-settings-datastore-public/build.gradle.kts`
- Test: `app/data/data-settings-datastore/data-settings-datastore-public/src/test/kotlin/com/hedvig/android/data/settings/datastore/SettingsDataStoreImplTest.kt`

**Interfaces:**
- Consumes: existing `SettingsDataStore` interface, `DataStore<Preferences>`.
- Produces: `enum class AnalyticsConsent { NOT_DECIDED, GRANTED, DENIED }`; `SettingsDataStore.setAnalyticsConsent(consent: AnalyticsConsent)` (suspend); `SettingsDataStore.observeAnalyticsConsent(): Flow<AnalyticsConsent>` (emits `NOT_DECIDED` when unset). Tasks 2, 3, and 12 rely on these exact names.

- [ ] **Step 1: Create the enum**

`AnalyticsConsent.kt`:

```kotlin
package com.hedvig.android.data.settings.datastore

/**
 * Consent for product analytics (Firebase Analytics only; Datadog performance/bug analytics is
 * intentionally not covered by this and stays always-on).
 * [NOT_DECIDED] means the member has never been asked or never answered: events are buffered
 * in-app, not forwarded, until an explicit decision is made.
 */
enum class AnalyticsConsent {
  NOT_DECIDED,
  GRANTED,
  DENIED,
}
```

- [ ] **Step 2: Write the failing test**

The module has no test source set yet. Add test deps to `build.gradle.kts` (append inside the existing `dependencies {}` block):

```kotlin
testImplementation(libs.assertK)
testImplementation(libs.coroutines.test)
testImplementation(libs.junit)
```

`SettingsDataStoreImplTest.kt`:

```kotlin
package com.hedvig.android.data.settings.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import assertk.assertThat
import assertk.assertions.isEqualTo
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsDataStoreImplTest {
  @get:Rule
  val temporaryFolder: TemporaryFolder = TemporaryFolder()

  // Note: pass a path that does NOT exist yet; DataStore treats a pre-created empty file as corrupt.
  private fun settingsDataStore(fileName: String): SettingsDataStoreImpl {
    val file = File(temporaryFolder.root, fileName)
    return SettingsDataStoreImpl(
      PreferenceDataStoreFactory.createWithPath(produceFile = { file.absolutePath.toPath() }),
    )
  }

  @Test
  fun `analytics consent defaults to NOT_DECIDED when nothing is stored`() = runTest {
    val store = settingsDataStore("settings1.preferences_pb")
    assertThat(store.observeAnalyticsConsent().first()).isEqualTo(AnalyticsConsent.NOT_DECIDED)
  }

  @Test
  fun `analytics consent round-trips GRANTED and DENIED`() = runTest {
    val store = settingsDataStore("settings2.preferences_pb")
    store.setAnalyticsConsent(AnalyticsConsent.GRANTED)
    assertThat(store.observeAnalyticsConsent().first()).isEqualTo(AnalyticsConsent.GRANTED)
    store.setAnalyticsConsent(AnalyticsConsent.DENIED)
    assertThat(store.observeAnalyticsConsent().first()).isEqualTo(AnalyticsConsent.DENIED)
  }
}
```

Note: `SettingsDataStoreImpl` is `internal`, so the test (same module) can construct it directly.

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :data-settings-datastore-public:test 2>&1 | grep -E "(BUILD|Test.*FAILED|error:)"`
Expected: compilation error, `observeAnalyticsConsent` unresolved.

- [ ] **Step 4: Implement**

In `SettingsDataStoreImpl.kt`, add to the `SettingsDataStore` interface:

```kotlin
  suspend fun setAnalyticsConsent(consent: AnalyticsConsent)

  /**
   * The member's product analytics consent decision. [AnalyticsConsent.NOT_DECIDED] when they
   * have not made an explicit choice yet.
   */
  fun observeAnalyticsConsent(): Flow<AnalyticsConsent>
```

Add to `SettingsDataStoreImpl`:

```kotlin
  override suspend fun setAnalyticsConsent(consent: AnalyticsConsent) {
    dataStore.edit {
      it[analyticsConsentKey] = consent.name
    }
  }

  override fun observeAnalyticsConsent(): Flow<AnalyticsConsent> {
    return dataStore.data.map { preferences ->
      preferences[analyticsConsentKey]
        ?.let { stored -> AnalyticsConsent.entries.firstOrNull { it.name == stored } }
        ?: AnalyticsConsent.NOT_DECIDED
    }
  }
```

And in its `companion object`:

```kotlin
    private val analyticsConsentKey = stringPreferencesKey(
      "com.hedvig.android.data.settings.datastore.settings-analytics-consent",
    )
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :data-settings-datastore-public:test 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Format and commit**

```bash
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/data/data-settings-datastore
git commit -m "Add tri-state AnalyticsConsent to SettingsDataStore"
```

---

### Task 2: Consent-aware Firebase tracking client (buffer-and-flush)

**Files:**
- Modify: `app/tracking/tracking-firebase/src/main/kotlin/com/hedvig/android/tracking/firebase/FirebaseEventTrackingClient.kt` (remove `@ContributesBinding(AppScope::class)` only; keep `@SingleIn`, `@Inject`, `internal`)
- Create: `app/tracking/tracking-firebase/src/main/kotlin/com/hedvig/android/tracking/firebase/ConsentAwareEventTrackingClient.kt`
- Modify: `app/tracking/tracking-firebase/build.gradle.kts`
- Test: `app/tracking/tracking-firebase/src/test/kotlin/com/hedvig/android/tracking/firebase/ConsentAwareEventTrackingClientTest.kt`

**Interfaces:**
- Consumes: `EventTrackingClient` (`com.hedvig.android.core.tracking`, methods `setCollectionEnabled`, `trackEvent(name, parameters)`, `trackScreen(name, screenClass, parameters)`, `setUserId`, `setUserProperty`), `SettingsDataStore.observeAnalyticsConsent()` from Task 1, `ApplicationScope` (`com.hedvig.android.core.common.ApplicationScope`).
- Produces: `ConsentAwareEventTrackingClient`, the sole `@ContributesBinding(AppScope::class)` for `EventTrackingClient`. Constructor: `(delegate: FirebaseEventTrackingClient, settingsDataStore: SettingsDataStore, applicationScope: ApplicationScope, @IoDispatcher coroutineContext: CoroutineContext)`. Behavior contract: NOT_DECIDED buffers events/screens (cap 200, oldest dropped), GRANTED flushes buffer (each replayed event gains a `buffered_at_epoch_ms` param) then forwards live, DENIED drops buffer and all subsequent events. `setUserId`/`setUserProperty`/`setCollectionEnabled` always pass through (demo-mode gating in `EventTrackingInitializer` keeps working unchanged).

- [ ] **Step 1: Add deps to `tracking-firebase/build.gradle.kts`**

Inside `dependencies {}` add:

```kotlin
implementation(projects.dataSettingsDatastorePublic)
testImplementation(libs.assertK)
testImplementation(libs.coroutines.test)
testImplementation(libs.junit)
testImplementation(libs.turbine)
```

- [ ] **Step 2: Write the failing test**

The test needs a testable seam for the consent flow and the delegate, so the class under test takes the `SettingsDataStore` interface (fakeable) and `EventTrackingClient` delegate typed parameter in a secondary visible-for-testing constructor. Simplest: make the primary constructor take the interface-typed delegate and have Metro inject the concrete one. Write exactly this:

`ConsentAwareEventTrackingClientTest.kt`:

```kotlin
package com.hedvig.android.tracking.firebase

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.key
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ConsentAwareEventTrackingClientTest {
  private class RecordingClient : EventTrackingClient {
    val events = mutableListOf<Pair<String, Map<String, Any?>>>()
    val screens = mutableListOf<String>()
    var userId: String? = null

    override fun setCollectionEnabled(enabled: Boolean) {}

    override fun trackEvent(name: String, parameters: Map<String, Any?>) {
      events.add(name to parameters)
    }

    override fun trackScreen(name: String, screenClass: String?, parameters: Map<String, Any?>) {
      screens.add(name)
    }

    override fun setUserId(userId: String?) {
      this.userId = userId
    }

    override fun setUserProperty(name: String, value: String?) {}
  }

  private class FakeSettingsDataStore(
    val consent: MutableStateFlow<AnalyticsConsent> = MutableStateFlow(AnalyticsConsent.NOT_DECIDED),
  ) : SettingsDataStore {
    override suspend fun setTheme(theme: Theme) = error("unused")

    override fun observeTheme(): Flow<Theme?> = error("unused")

    override suspend fun setEmailSubscriptionPreference(subscribe: Boolean) = error("unused")

    override fun observeEmailSubscriptionPreference(): Flow<Boolean> = error("unused")

    override suspend fun setAnalyticsConsent(newConsent: AnalyticsConsent) {
      consent.value = newConsent
    }

    override fun observeAnalyticsConsent(): Flow<AnalyticsConsent> = consent
  }

  private fun TestScope.client(
    recording: RecordingClient,
    settings: FakeSettingsDataStore,
  ): ConsentAwareEventTrackingClient {
    return ConsentAwareEventTrackingClient(
      delegate = recording,
      settingsDataStore = settings,
      applicationScope = backgroundScope,
      coroutineContext = StandardTestDispatcher(testScheduler),
      clock = { 1234L },
    )
  }

  @Test
  fun `while NOT_DECIDED events are buffered, not forwarded`() = runTest {
    val recording = RecordingClient()
    val client = client(recording, FakeSettingsDataStore())
    runCurrent()
    client.trackEvent("clicked_thing", mapOf("k" to "v"))
    runCurrent()
    assertThat(recording.events).isEmpty()
  }

  @Test
  fun `granting consent flushes buffered events with buffered_at_epoch_ms and forwards live ones`() = runTest {
    val recording = RecordingClient()
    val settings = FakeSettingsDataStore()
    val client = client(recording, settings)
    runCurrent()
    client.trackEvent("early_event", emptyMap())
    settings.consent.value = AnalyticsConsent.GRANTED
    runCurrent()
    assertThat(recording.events.map { it.first }).containsExactly("early_event")
    assertThat(recording.events.single().second).key("buffered_at_epoch_ms").isEqualTo(1234L)
    client.trackEvent("live_event", emptyMap())
    assertThat(recording.events.map { it.first }).containsExactly("early_event", "live_event")
  }

  @Test
  fun `denying consent drops the buffer and all subsequent events`() = runTest {
    val recording = RecordingClient()
    val settings = FakeSettingsDataStore()
    val client = client(recording, settings)
    runCurrent()
    client.trackEvent("early_event", emptyMap())
    settings.consent.value = AnalyticsConsent.DENIED
    runCurrent()
    client.trackEvent("post_denial_event", emptyMap())
    runCurrent()
    assertThat(recording.events).isEmpty()
  }

  @Test
  fun `setUserId passes through regardless of consent`() = runTest {
    val recording = RecordingClient()
    val client = client(recording, FakeSettingsDataStore())
    runCurrent()
    client.setUserId("member-id")
    assertThat(recording.userId).isEqualTo("member-id")
  }
}
```

Note: `FakeSettingsDataStore` must override every `SettingsDataStore` member; if Task 3 has already added members by the time this compiles, add `error("unused")` overrides for those too. The `applicationScope` parameter is typed `CoroutineScope` in the constructor below, so passing `backgroundScope` works; production wiring passes `ApplicationScope` (which is a `CoroutineScope`).

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :tracking-firebase:testDebugUnitTest 2>&1 | grep -E "(BUILD|error:)" | head -20`
Expected: compilation failure, `ConsentAwareEventTrackingClient` unresolved.

- [ ] **Step 4: Implement**

First, in `FirebaseEventTrackingClient.kt`, delete the line `@ContributesBinding(AppScope::class)` and its now-unused import. Keep `@SingleIn(AppScope::class)` and `@Inject`.

`ConsentAwareEventTrackingClient.kt`:

```kotlin
package com.hedvig.android.tracking.firebase

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.IoDispatcher
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Gates product analytics behind the member's [AnalyticsConsent]:
 * - [AnalyticsConsent.NOT_DECIDED]: events/screens are buffered in-memory (bounded), not forwarded.
 * - [AnalyticsConsent.GRANTED]: the buffer is flushed (stamped with `buffered_at_epoch_ms`) and
 *   subsequent events forward live.
 * - [AnalyticsConsent.DENIED]: the buffer is dropped and nothing is forwarded.
 * Identity calls ([setUserId], [setUserProperty]) and [setCollectionEnabled] (demo-mode gating)
 * always pass through. Datadog is untouched by design: it carries performance and bug analytics,
 * not product analytics.
 *
 * Deliberately NOT annotated with Metro annotations: annotating it `@ContributesBinding` for
 * [EventTrackingClient] while also injecting an [EventTrackingClient] delegate would self-loop.
 * The binding is supplied by [ConsentAwareEventTrackingClientProviders] below, which hands in the
 * concrete [FirebaseEventTrackingClient] as the delegate.
 */
internal class ConsentAwareEventTrackingClient(
  private val delegate: EventTrackingClient,
  private val settingsDataStore: SettingsDataStore,
  applicationScope: CoroutineScope,
  @IoDispatcher coroutineContext: CoroutineContext,
  private val clock: () -> Long = { System.currentTimeMillis() },
) : EventTrackingClient {
  private val lock = Any()
  private var consent: AnalyticsConsent = AnalyticsConsent.NOT_DECIDED
  private val buffer = ArrayDeque<BufferedCall>()

  init {
    applicationScope.launch(coroutineContext) {
      settingsDataStore.observeAnalyticsConsent().collect { newConsent ->
        val toFlush: List<BufferedCall> = synchronized(lock) {
          consent = newConsent
          when (newConsent) {
            AnalyticsConsent.GRANTED -> buffer.toList().also { buffer.clear() }
            AnalyticsConsent.DENIED -> {
              buffer.clear()
              emptyList()
            }
            AnalyticsConsent.NOT_DECIDED -> emptyList()
          }
        }
        if (toFlush.isNotEmpty()) {
          logcat { "Analytics consent granted, flushing ${toFlush.size} buffered events" }
          for (call in toFlush) {
            call.forwardTo(delegate)
          }
        }
      }
    }
  }

  override fun setCollectionEnabled(enabled: Boolean) {
    delegate.setCollectionEnabled(enabled)
  }

  override fun trackEvent(name: String, parameters: Map<String, Any?>) {
    handle(BufferedCall.Event(name, parameters, clock()))
  }

  override fun trackScreen(name: String, screenClass: String?, parameters: Map<String, Any?>) {
    handle(BufferedCall.Screen(name, screenClass, parameters, clock()))
  }

  override fun setUserId(userId: String?) {
    delegate.setUserId(userId)
  }

  override fun setUserProperty(name: String, value: String?) {
    delegate.setUserProperty(name, value)
  }

  private fun handle(call: BufferedCall) {
    val action: Action = synchronized(lock) {
      when (consent) {
        AnalyticsConsent.GRANTED -> Action.Forward
        AnalyticsConsent.DENIED -> Action.Drop
        AnalyticsConsent.NOT_DECIDED -> {
          buffer.addLast(call)
          while (buffer.size > MAX_BUFFERED_CALLS) {
            buffer.removeFirst()
          }
          Action.Drop
        }
      }
    }
    if (action == Action.Forward) {
      call.forwardTo(delegate, includeBufferedAt = false)
    }
  }

  private enum class Action { Forward, Drop }

  private sealed interface BufferedCall {
    val bufferedAtEpochMs: Long

    fun forwardTo(client: EventTrackingClient, includeBufferedAt: Boolean = true)

    data class Event(
      val name: String,
      val parameters: Map<String, Any?>,
      override val bufferedAtEpochMs: Long,
    ) : BufferedCall {
      override fun forwardTo(client: EventTrackingClient, includeBufferedAt: Boolean) {
        val params = if (includeBufferedAt) parameters + ("buffered_at_epoch_ms" to bufferedAtEpochMs) else parameters
        client.trackEvent(name, params)
      }
    }

    data class Screen(
      val name: String,
      val screenClass: String?,
      val parameters: Map<String, Any?>,
      override val bufferedAtEpochMs: Long,
    ) : BufferedCall {
      override fun forwardTo(client: EventTrackingClient, includeBufferedAt: Boolean) {
        val params = if (includeBufferedAt) parameters + ("buffered_at_epoch_ms" to bufferedAtEpochMs) else parameters
        client.trackScreen(name, screenClass, params)
      }
    }
  }

  companion object {
    private const val MAX_BUFFERED_CALLS = 200
  }
}
```

In the same file, below the class, add the providers interface that supplies the binding (adjust the file's import list accordingly: `dev.zacsweers.metro.ContributesTo`, `dev.zacsweers.metro.Provides`, `com.hedvig.android.core.common.ApplicationScope`; remove the now-unused `dev.zacsweers.metro.Inject` / `dev.zacsweers.metro.ContributesBinding` imports if present):

```kotlin
@ContributesTo(AppScope::class)
internal interface ConsentAwareEventTrackingClientProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideEventTrackingClient(
    firebaseEventTrackingClient: FirebaseEventTrackingClient,
    settingsDataStore: SettingsDataStore,
    applicationScope: ApplicationScope,
    @IoDispatcher coroutineContext: CoroutineContext,
  ): EventTrackingClient = ConsentAwareEventTrackingClient(
    delegate = firebaseEventTrackingClient,
    settingsDataStore = settingsDataStore,
    applicationScope = applicationScope,
    coroutineContext = coroutineContext,
  )
}
```

The class keeps the default-argument `clock` parameter so tests can pin time; production wiring never passes it.

- [ ] **Step 5: Run tests**

Run: `./gradlew :tracking-firebase:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED|error:)"`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Verify the app graph still compiles (binding swap is graph-visible)**

Run: `./gradlew :app:compileDevelopDebugKotlin 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED|error:)" | head -5`
Expected: `BUILD SUCCESSFUL`. If Metro reports a duplicate or missing `EventTrackingClient` binding, re-check that `FirebaseEventTrackingClient` no longer has `@ContributesBinding` and only the `@Provides` above supplies the interface.
(If `:app:compileDevelopDebugKotlin` is not a valid task name, list variants with `./gradlew :app:tasks --all | grep -i compile | head` and use the develop-debug variant.)

- [ ] **Step 7: Format and commit**

```bash
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/tracking/tracking-firebase
git commit -m "Gate Firebase Analytics behind tri-state consent with buffer-and-flush"
```

---

### Task 3: Analytics consent toggle row in profile Settings

**Files:**
- Modify: `app/feature/feature-profile/src/main/kotlin/com/hedvig/android/feature/profile/settings/SettingsPresenter.kt`
- Modify: `app/feature/feature-profile/src/main/kotlin/com/hedvig/android/feature/profile/settings/SettingsDestination.kt`

**Interfaces:**
- Consumes: `SettingsDataStore.observeAnalyticsConsent()` / `setAnalyticsConsent(...)` (Task 1), existing `SettingsUiState`/`SettingsEvent` shapes, `HedvigBigCard(onClick, inputText, labelText, modifier)` (used verbatim by the existing Notifications row).
- Produces: `SettingsEvent.ChangeAnalyticsConsent(val consent: AnalyticsConsent)`, `SettingsUiState.analyticsConsent: AnalyticsConsent?` on both `Loading` (always null) and `Loaded`.

No new presenter test: `SettingsPresenter` has a wide constructor and this change is two straight datastore pass-throughs; the datastore behavior itself is covered by Task 1's tests. Compile plus existing module tests gate this task.

- [ ] **Step 1: Extend UiState and Events**

In `SettingsPresenter.kt`:

- Add import `com.hedvig.android.data.settings.datastore.AnalyticsConsent`.
- In `sealed interface SettingsUiState` add `val analyticsConsent: AnalyticsConsent?` to the interface; in `Loading` add `override val analyticsConsent: AnalyticsConsent? = null`; in `Loaded` add `override val analyticsConsent: AnalyticsConsent?` as a constructor property.
- In `sealed interface SettingsEvent` add:

```kotlin
  data class ChangeAnalyticsConsent(val consent: AnalyticsConsent) : SettingsEvent
```

- [ ] **Step 2: Wire the presenter**

In `SettingsPresenter.present(...)`, next to the other `collectAsState` lines:

```kotlin
    val analyticsConsent = settingsDataStore.observeAnalyticsConsent()
      .collectAsState(lastState.analyticsConsent).value
```

In `CollectEvents`' `when` add:

```kotlin
        is SettingsEvent.ChangeAnalyticsConsent -> {
          launch { settingsDataStore.setAnalyticsConsent(event.consent) }
        }
```

Pass `analyticsConsent = analyticsConsent` into the returned `SettingsUiState.Loaded(...)` construction.

- [ ] **Step 3: Add the row to the settings screen**

In `SettingsDestination.kt`, inside the `Loaded` branch after the `EmailSubscriptionWithDialog` block (and its trailing spacer), add a row following the existing Notifications-row pattern:

```kotlin
        Spacer(Modifier.height(4.dp))
        HedvigBigCard(
          onClick = {
            val newConsent = if (uiState.analyticsConsent == AnalyticsConsent.GRANTED) {
              AnalyticsConsent.DENIED
            } else {
              AnalyticsConsent.GRANTED
            }
            onChangeAnalyticsConsent(newConsent)
          },
          inputText = if (uiState.analyticsConsent == AnalyticsConsent.GRANTED) {
            // TODO: Add "On" / "På" to Lokalise
            "On"
          } else {
            // NOT_DECIDED renders as "Off": nothing is forwarded to Firebase in that state.
            // TODO: Add "Off" / "Av" to Lokalise
            "Off"
          },
          // TODO: Add "Product analytics" / "Produktanalys" to Lokalise
          labelText = "Product analytics",
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
```

Thread a new `onChangeAnalyticsConsent: (AnalyticsConsent) -> Unit` parameter through `SettingsScreen(...)` from `SettingsDestination(...)`'s viewModel call site: `onChangeAnalyticsConsent = { viewModel.emit(SettingsEvent.ChangeAnalyticsConsent(it)) }`. Match how `changeSubscriptionPreference` is threaded today. Add the `AnalyticsConsent` import.

- [ ] **Step 4: Compile and test the module**

Run: `./gradlew :feature-profile:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED|error:)" | head -5`
Expected: `BUILD SUCCESSFUL`. If an existing `SettingsPresenterTest` constructs `SettingsUiState.Loaded`, add `analyticsConsent = null` to those constructor calls.

- [ ] **Step 5: Format and commit**

```bash
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-profile
git commit -m "Add product analytics consent toggle to settings"
```

---

### Task 4: feature-onboarding module scaffold and nav keys

**Files:**
- Create: `app/feature/feature-onboarding/build.gradle.kts`
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/navigation/OnboardingKeys.kt`

**Interfaces:**
- Produces: `OnboardingKey` (public `data object`, the flow root/welcome), `OnboardingStepId` (public enum: `AnalyticsConsent, PhoneNumber, Theme, CoInsured, PetIds, InviteFriend, ConnectPayment, BundleDiscount`), `OnboardingStepKey(stepId: OnboardingStepId)` (internal). All later tasks use these exact names. Note: `Welcome` is NOT an `OnboardingStepId`; the welcome screen is `OnboardingKey` itself. The progress bar counts welcome as position 0 (see Task 6's path model).

The module is auto-discovered by `settings.gradle.kts` (any dir under `app/` with a `build.gradle.kts`).

- [ ] **Step 1: Create `build.gradle.kts`**

```kotlin
plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
  navKeys()
  viewModels()
}

dependencies {
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.jetbrains.lifecycle.runtime.compose)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.authCorePublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.dataSettingsDatastorePublic)
  implementation(projects.designSystemHedvig)
  implementation(projects.featureConnectPaymentTrustlyNavigation)
  implementation(projects.featureEditCoinsuredNavigation)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.theme)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.moleculeTest)
}
```

(Modeled on `app/feature/feature-chip-id/build.gradle.kts`. `ChipIdKey` is deliberately absent: `feature-chip-id` is not a `-navigation` module, so navigation to it is threaded from `:app` as a lambda in Task 19.)

- [ ] **Step 2: Create the keys**

`OnboardingKeys.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

/**
 * The onboarding flow root, rendering the welcome step. Pushed on top of Home by the onboarding
 * gate in `:app` for members who have not seen onboarding yet.
 */
@androidx.annotation.Keep
@Serializable
data object OnboardingKey : HedvigNavKey

/**
 * One entry per onboarding step after welcome. Deliberately tiny: the eagerly-fetched flow data
 * lives in the ActivityRetainedScope OnboardingSessionStore, not in the key, so back-stack
 * persistence stays cheap and process-death restore re-fetches instead of deserializing a blob.
 */
@androidx.annotation.Keep
@Serializable
internal data class OnboardingStepKey(
  val stepId: OnboardingStepId,
) : HedvigNavKey

/**
 * Identifies each onboarding step past welcome. The member's concrete path (which of these apply,
 * in order) is computed by buildOnboardingPath from the eagerly fetched OnboardingData.
 */
enum class OnboardingStepId {
  AnalyticsConsent,
  PhoneNumber,
  Theme,
  CoInsured,
  PetIds,
  InviteFriend,
  ConnectPayment,
  BundleDiscount,
}
```

- [ ] **Step 3: Verify the module compiles**

Run: `./gradlew :feature-onboarding:compileDebugKotlin 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED|error:)" | head -5`
Expected: `BUILD SUCCESSFUL`.

Note: `ExhaustiveBackStackSerializationTest` in `:app` discovers keys by classpath scanning; these keys only enter its scope when `:app` gains the dependency in Task 19, so no action here.

- [ ] **Step 4: Format and commit**

```bash
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Scaffold feature-onboarding module with nav keys"
```

---

### Task 5: OnboardingQuery, project-owned data models, repository

**Files:**
- Create: `app/feature/feature-onboarding/src/main/graphql/OnboardingQuery.graphql`
- Create: `app/feature/feature-onboarding/src/main/graphql/OnboardingUpdateContactInfoMutation.graphql`
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/data/OnboardingData.kt`
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/data/OnboardingRepository.kt`

**Interfaces:**
- Consumes: `ApolloClient` (unqualified, `AppScope`), `safeExecute(::ErrorMessage)` from `com.hedvig.android.apollo`, `ErrorMessage` from `com.hedvig.android.core.common`.
- Produces:
  - `OnboardingData(email, phoneNumber, contracts, referralInformation, hasConnectedPayinMethod, crossSells)` with derived `contractsWithMissingCoInsured`, `contractsWithMissingPetId`, `hasOnlyAccidentContracts`.
  - `OnboardingContract(id, displayName, exposureName, typeOfContract, missingCoInsuredCount, isMissingPetId)`.
  - `OnboardingReferralInformation(code, monthlyDiscountPerReferralAmount: Double, currencyCode: String)`.
  - `OnboardingCrossSell(id, title, description, storeUrl)`.
  - `OnboardingRepository` with `suspend fun getOnboardingData(): Either<ErrorMessage, OnboardingData>` and `suspend fun updateContactInfo(email: String, phoneNumber: String): Either<ErrorMessage, Unit>`.

All types `internal`; no `octopus.*` type escapes the repository impl. Derivation logic (missing filters, accident-only) lives on `OnboardingData` as pure code so Task 6's tests cover it without Apollo types.

- [ ] **Step 1: Write the query**

`OnboardingQuery.graphql`:

```graphql
query Onboarding {
  currentMember {
    id
    email
    phoneNumber
    activeContracts {
      id
      exposureDisplayNameShort
      isMissingPetId
      coInsured {
        hasMissingInfo
      }
      currentAgreement {
        productVariant {
          typeOfContract
          displayName
        }
      }
    }
    referralInformation {
      code
      monthlyDiscountPerReferral {
        amount
        currencyCode
      }
    }
    paymentMethods {
      payinMethods {
        status
      }
    }
    crossSellV2(input: { userFlow: HOME_X_SELL, flowSource: null, experiments: [] }) {
      otherCrossSells {
        id
        title
        description
        storeUrl
      }
    }
  }
}
```

(`userFlow: HOME_X_SELL` mirrors `feature-home`'s existing `crossSellV2` usage; onboarding is entered from Home. If codegen complains about a field's exact shape, check the schema at `app/apollo/apollo-octopus-public/src/commonMain/graphql/com/hedvig/android/apollo/octopus/schema.graphqls` and adjust the selection, never the Kotlin-facing model shapes.)

`OnboardingUpdateContactInfoMutation.graphql`:

```graphql
mutation OnboardingUpdateContactInfo($email: String!, $phoneNumber: String!) {
  memberUpdateContactInfo(input: { email: $email, phoneNumber: $phoneNumber }) {
    userError {
      message
    }
    member {
      id
      phoneNumber
    }
  }
}
```

- [ ] **Step 2: Create the models**

`OnboardingData.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.data

/**
 * Everything the onboarding flow needs, fetched eagerly in one round trip before the flow is
 * shown, so the progress bar length is known up-front and no step blocks on network to render.
 */
internal data class OnboardingData(
  val email: String,
  val phoneNumber: String?,
  val contracts: List<OnboardingContract>,
  val referralInformation: OnboardingReferralInformation?,
  val hasConnectedPayinMethod: Boolean,
  val crossSells: List<OnboardingCrossSell>,
) {
  val contractsWithMissingCoInsured: List<OnboardingContract> =
    contracts.filter { it.missingCoInsuredCount > 0 }

  val contractsWithMissingPetId: List<OnboardingContract> =
    contracts.filter { it.isMissingPetId }

  val hasOnlyAccidentContracts: Boolean =
    contracts.isNotEmpty() && contracts.all { it.typeOfContract.contains("ACCIDENT") }
}

internal data class OnboardingContract(
  val id: String,
  val displayName: String,
  val exposureName: String,
  val typeOfContract: String,
  val missingCoInsuredCount: Int,
  val isMissingPetId: Boolean,
)

internal data class OnboardingReferralInformation(
  val code: String,
  val monthlyDiscountPerReferralAmount: Double,
  val currencyCode: String,
)

internal data class OnboardingCrossSell(
  val id: String,
  val title: String,
  val description: String,
  val storeUrl: String,
)
```

- [ ] **Step 3: Create the repository**

`OnboardingRepository.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import octopus.OnboardingQuery
import octopus.OnboardingUpdateContactInfoMutation

internal interface OnboardingRepository {
  suspend fun getOnboardingData(): Either<ErrorMessage, OnboardingData>

  suspend fun updateContactInfo(email: String, phoneNumber: String): Either<ErrorMessage, Unit>
}

@ContributesBinding(AppScope::class)
@Inject
internal class OnboardingRepositoryImpl(
  private val apolloClient: ApolloClient,
) : OnboardingRepository {
  override suspend fun getOnboardingData(): Either<ErrorMessage, OnboardingData> = either {
    val member = apolloClient
      .query(OnboardingQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute(::ErrorMessage)
      .bind()
      .currentMember
    OnboardingData(
      email = member.email,
      phoneNumber = member.phoneNumber,
      contracts = member.activeContracts.map { contract ->
        OnboardingContract(
          id = contract.id,
          displayName = contract.currentAgreement.productVariant.displayName,
          exposureName = contract.exposureDisplayNameShort,
          typeOfContract = contract.currentAgreement.productVariant.typeOfContract,
          missingCoInsuredCount = contract.coInsured.orEmpty().count { it.hasMissingInfo },
          isMissingPetId = contract.isMissingPetId,
        )
      },
      referralInformation = member.referralInformation.let { referralInformation ->
        OnboardingReferralInformation(
          code = referralInformation.code,
          monthlyDiscountPerReferralAmount = referralInformation.monthlyDiscountPerReferral.amount,
          currencyCode = referralInformation.monthlyDiscountPerReferral.currencyCode.rawValue,
        )
      },
      hasConnectedPayinMethod = member.paymentMethods.payinMethods.isNotEmpty(),
      crossSells = member.crossSellV2.otherCrossSells.map { crossSell ->
        OnboardingCrossSell(
          id = crossSell.id,
          title = crossSell.title,
          description = crossSell.description,
          storeUrl = crossSell.storeUrl,
        )
      },
    )
  }

  override suspend fun updateContactInfo(email: String, phoneNumber: String): Either<ErrorMessage, Unit> = either {
    val result = apolloClient
      .mutation(OnboardingUpdateContactInfoMutation(email = email, phoneNumber = phoneNumber))
      .safeExecute(::ErrorMessage)
      .bind()
    val userError = result.memberUpdateContactInfo.userError
    ensure(userError == null) { ErrorMessage(userError?.message) }
  }
}
```

Adjust generated accessor shapes to whatever `generateApolloSources` produces (nullability of `id` as `UUID`/String, `currencyCode.rawValue` vs `.name`, `contract.id` may need `.toString()` since the schema type is `UUID!`). The rule: fix the impl's mapping lines, never leak the generated types past this file. If `referralInformation` turns out nullable in codegen, map null to `referralInformation = null`.

- [ ] **Step 4: Generate Apollo sources and compile**

Run: `./gradlew :feature-onboarding:generateApolloSources :feature-onboarding:compileDebugKotlin 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED|error:|e:)" | head -20`
Expected: `BUILD SUCCESSFUL`. Iterate on the mapping lines per the note above until clean.

- [ ] **Step 5: Format and commit**

```bash
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding query, data models and repository"
```

---

### Task 6: buildOnboardingPath pure function

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/data/OnboardingPath.kt`
- Test: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/data/OnboardingPathTest.kt`

**Interfaces:**
- Consumes: `OnboardingData` (Task 5), `OnboardingStepId` (Task 4).
- Produces: `internal fun buildOnboardingPath(data: OnboardingData): List<OnboardingStepId>`. Welcome is not in the list (it's `OnboardingKey`); the progress bar's total segment count is `path.size + 1` and a step's 0-based position is `path.indexOf(stepId) + 1` (welcome occupies position 0). Tasks 8-19 rely on this convention.

- [ ] **Step 1: Write the failing tests**

`OnboardingPathTest.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.data

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.doesNotContain
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import org.junit.Test

class OnboardingPathTest {
  private fun contract(
    id: String = "contract-1",
    typeOfContract: String = "SE_APARTMENT_RENT",
    missingCoInsuredCount: Int = 0,
    isMissingPetId: Boolean = false,
  ) = OnboardingContract(
    id = id,
    displayName = "Home Insurance",
    exposureName = "Bellmansgatan 19A",
    typeOfContract = typeOfContract,
    missingCoInsuredCount = missingCoInsuredCount,
    isMissingPetId = isMissingPetId,
  )

  private fun data(
    contracts: List<OnboardingContract> = listOf(contract()),
    referralInformation: OnboardingReferralInformation? = OnboardingReferralInformation("CODE", 10.0, "SEK"),
    hasConnectedPayinMethod: Boolean = false,
    crossSells: List<OnboardingCrossSell> = listOf(OnboardingCrossSell("cs", "Pet", "For your pet", "https://x")),
  ) = OnboardingData(
    email = "member@example.com",
    phoneNumber = "070 990 12 32",
    contracts = contracts,
    referralInformation = referralInformation,
    hasConnectedPayinMethod = hasConnectedPayinMethod,
    crossSells = crossSells,
  )

  @Test
  fun `consent, phone and theme are always present, in order, at the front`() {
    val path = buildOnboardingPath(data())
    assertThat(path.take(3)).containsExactly(
      OnboardingStepId.AnalyticsConsent,
      OnboardingStepId.PhoneNumber,
      OnboardingStepId.Theme,
    )
  }

  @Test
  fun `everything applicable produces the full path in canonical order`() {
    val path = buildOnboardingPath(
      data(contracts = listOf(contract(missingCoInsuredCount = 1, isMissingPetId = true))),
    )
    assertThat(path).containsExactly(
      OnboardingStepId.AnalyticsConsent,
      OnboardingStepId.PhoneNumber,
      OnboardingStepId.Theme,
      OnboardingStepId.CoInsured,
      OnboardingStepId.PetIds,
      OnboardingStepId.InviteFriend,
      OnboardingStepId.ConnectPayment,
      OnboardingStepId.BundleDiscount,
    )
  }

  @Test
  fun `co-insured step is skipped when no contract has missing co-insured info`() {
    val path = buildOnboardingPath(data(contracts = listOf(contract(missingCoInsuredCount = 0))))
    assertThat(path).doesNotContain(OnboardingStepId.CoInsured)
  }

  @Test
  fun `pet id step is skipped when no contract is missing a pet id`() {
    val path = buildOnboardingPath(data(contracts = listOf(contract(isMissingPetId = false))))
    assertThat(path).doesNotContain(OnboardingStepId.PetIds)
  }

  @Test
  fun `invite step is skipped without referral information`() {
    val path = buildOnboardingPath(data(referralInformation = null))
    assertThat(path).doesNotContain(OnboardingStepId.InviteFriend)
  }

  @Test
  fun `connect payment step is skipped when a payin method is already connected`() {
    val path = buildOnboardingPath(data(hasConnectedPayinMethod = true))
    assertThat(path).doesNotContain(OnboardingStepId.ConnectPayment)
  }

  @Test
  fun `bundle step is skipped when there are no cross sells`() {
    val path = buildOnboardingPath(data(crossSells = emptyList()))
    assertThat(path).doesNotContain(OnboardingStepId.BundleDiscount)
  }

  @Test
  fun `bundle step is skipped for accident-only members even with cross sells`() {
    val path = buildOnboardingPath(data(contracts = listOf(contract(typeOfContract = "SE_ACCIDENT"))))
    assertThat(path).doesNotContain(OnboardingStepId.BundleDiscount)
  }

  @Test
  fun `bundle step is present for a mixed portfolio that includes accident`() {
    val path = buildOnboardingPath(
      data(contracts = listOf(contract(typeOfContract = "SE_ACCIDENT"), contract(id = "c2"))),
    )
    assertThat(path).contains(OnboardingStepId.BundleDiscount)
  }
}
```

- [ ] **Step 2: Run to verify failure**

Run: `./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD|error:)" | head -5`
Expected: compile failure, `buildOnboardingPath` unresolved.

- [ ] **Step 3: Implement**

`OnboardingPath.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.data

import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId

/**
 * Computes which onboarding steps apply to this member, in presentation order. Pure so the skip
 * rules stay unit-testable. Welcome is not in the result: it is the flow root itself, occupying
 * progress position 0, so the progress bar renders `size + 1` segments.
 */
internal fun buildOnboardingPath(data: OnboardingData): List<OnboardingStepId> = buildList {
  add(OnboardingStepId.AnalyticsConsent)
  add(OnboardingStepId.PhoneNumber)
  add(OnboardingStepId.Theme)
  if (data.contractsWithMissingCoInsured.isNotEmpty()) {
    add(OnboardingStepId.CoInsured)
  }
  if (data.contractsWithMissingPetId.isNotEmpty()) {
    add(OnboardingStepId.PetIds)
  }
  if (data.referralInformation != null) {
    add(OnboardingStepId.InviteFriend)
  }
  if (!data.hasConnectedPayinMethod) {
    add(OnboardingStepId.ConnectPayment)
  }
  if (data.crossSells.isNotEmpty() && !data.hasOnlyAccidentContracts) {
    add(OnboardingStepId.BundleDiscount)
  }
}
```

- [ ] **Step 4: Run tests**

Run: `./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Format and commit**

```bash
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding path builder with per-step skip rules"
```

---

### Task 7: OnboardingSeenStore (per-member show-once flag)

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/data/OnboardingSeenStore.kt`
- Test: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/data/OnboardingSeenStoreTest.kt`

**Interfaces:**
- Consumes: `DataStore<Preferences>` (AppScope binding from `core-datastore-public`).
- Produces: `internal interface OnboardingSeenStore { suspend fun hasSeenOnboarding(memberId: String): Boolean; suspend fun markOnboardingSeen(memberId: String) }`. Member-id resolution is deliberately the caller's job (Tasks 8/19 hold `MemberIdService`), keeping this store pure and testable.

- [ ] **Step 1: Write the failing test**

`OnboardingSeenStoreTest.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OnboardingSeenStoreTest {
  @get:Rule
  val temporaryFolder: TemporaryFolder = TemporaryFolder()

  // Note: the path must NOT exist yet; DataStore treats a pre-created empty file as corrupt.
  private fun store(): OnboardingSeenStore = DataStoreOnboardingSeenStore(
    PreferenceDataStoreFactory.createWithPath(
      produceFile = { File(temporaryFolder.root, "seen.preferences_pb").absolutePath.toPath() },
    ),
  )

  @Test
  fun `a member who never saw onboarding reads false`() = runTest {
    assertThat(store().hasSeenOnboarding("123")).isFalse()
  }

  @Test
  fun `marking seen is per member`() = runTest {
    val store = store()
    store.markOnboardingSeen("123")
    assertThat(store.hasSeenOnboarding("123")).isTrue()
    assertThat(store.hasSeenOnboarding("456")).isFalse()
  }
}
```

- [ ] **Step 2: Run to verify failure**

Run: `./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD|error:)" | head -5`
Expected: compile failure.

- [ ] **Step 3: Implement**

`OnboardingSeenStore.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Remembers, per member and per install, that onboarding was shown. Set when the member completes
 * the flow or dismisses it with the close button; once set, onboarding never appears again for
 * that member on this device.
 */
internal interface OnboardingSeenStore {
  suspend fun hasSeenOnboarding(memberId: String): Boolean

  suspend fun markOnboardingSeen(memberId: String)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class DataStoreOnboardingSeenStore(
  private val dataStore: DataStore<Preferences>,
) : OnboardingSeenStore {
  override suspend fun hasSeenOnboarding(memberId: String): Boolean {
    return dataStore.data.map { it[seenKey(memberId)] ?: false }.first()
  }

  override suspend fun markOnboardingSeen(memberId: String) {
    dataStore.edit { it[seenKey(memberId)] = true }
  }

  private fun seenKey(memberId: String): Preferences.Key<Boolean> {
    return booleanPreferencesKey("com.hedvig.android.feature.onboarding.seen.$memberId")
  }
}
```

- [ ] **Step 4: Run tests, format, commit**

```bash
./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add per-member onboarding seen store"
```

---

### Task 8: OnboardingSessionStore, CompleteOnboardingUseCase, OnboardingNavigator

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/data/OnboardingSessionStore.kt`
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/data/CompleteOnboardingUseCase.kt`
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/navigation/OnboardingNavigator.kt`
- Test: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/navigation/OnboardingNavigatorTest.kt`
- Test fixture: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/FakeOnboardingRepository.kt`

**Interfaces:**
- Consumes: `OnboardingRepository`, `buildOnboardingPath`, `OnboardingSeenStore`, `MemberIdService` (`com.hedvig.android.auth`), `Backstack` + extensions (`add`, `removeAllOf`, `navigateUp` from `com.hedvig.android.navigation.compose`).
- Produces (used by every step presenter and by Task 19's gate):
  - `OnboardingSessionStore` (`@SingleIn(ActivityRetainedScope::class)`): `val currentSession: OnboardingSession?`; `suspend fun getOrFetchSession(): Either<ErrorMessage, OnboardingSession>`; `suspend fun refreshData(): Either<ErrorMessage, OnboardingSession>` (refetches data, KEEPS the original path so the progress bar never reshuffles mid-flow).
  - `OnboardingSession(data: OnboardingData, path: List<OnboardingStepId>)`.
  - `CompleteOnboardingUseCase` interface: `suspend fun invoke()` (resolves member id, marks seen; no-op if no member id).
  - `OnboardingNavigator` (`@SingleIn(ActivityRetainedScope::class)`): `suspend fun continueFrom(current: OnboardingStepId?)` (null = from welcome; unknown/last step exits), `suspend fun exitOnboarding()` (marks seen then removes all onboarding keys), `fun navigateBack()` (delegates to `backstack.navigateUp()`, used only by the top-bar arrow).

- [ ] **Step 1: Implement the session store**

`OnboardingSessionStore.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Holds the eagerly fetched onboarding data plus the computed step path for the lifetime of one
 * Activity. Keys stay tiny (see OnboardingStepKey); after process death this cache is empty and
 * the visible step re-fetches through [getOrFetchSession].
 */
@SingleIn(ActivityRetainedScope::class)
@Inject
internal class OnboardingSessionStore(
  private val onboardingRepository: OnboardingRepository,
) {
  private val mutex = Mutex()
  private var cachedSession: OnboardingSession? = null

  val currentSession: OnboardingSession?
    get() = cachedSession

  suspend fun getOrFetchSession(): Either<ErrorMessage, OnboardingSession> = mutex.withLock {
    cachedSession?.let { return@withLock it.right() }
    onboardingRepository.getOnboardingData().map { data ->
      OnboardingSession(data = data, path = buildOnboardingPath(data)).also { cachedSession = it }
    }
  }

  /**
   * Refetches the data (used after returning from an external flow like edit co-insured or
   * Trustly) but keeps the original path: the progress bar must not reshuffle mid-flow.
   */
  suspend fun refreshData(): Either<ErrorMessage, OnboardingSession> = mutex.withLock {
    val existing = cachedSession
      ?: return@withLock ErrorMessage("No onboarding session to refresh").left()
    onboardingRepository.getOnboardingData().map { data ->
      existing.copy(data = data).also { cachedSession = it }
    }
  }
}

internal data class OnboardingSession(
  val data: OnboardingData,
  val path: List<OnboardingStepId>,
)
```

- [ ] **Step 2: Implement CompleteOnboardingUseCase**

`CompleteOnboardingUseCase.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.data

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

internal interface CompleteOnboardingUseCase {
  suspend fun invoke()
}

@ContributesBinding(AppScope::class)
@Inject
internal class CompleteOnboardingUseCaseImpl(
  private val memberIdService: MemberIdService,
  private val onboardingSeenStore: OnboardingSeenStore,
) : CompleteOnboardingUseCase {
  override suspend fun invoke() {
    val memberId = memberIdService.getMemberId().first()
    if (memberId == null) {
      logcat(LogPriority.WARN) { "Completing onboarding without a member id; seen flag not stored" }
      return
    }
    onboardingSeenStore.markOnboardingSeen(memberId)
  }
}
```

- [ ] **Step 3: Write the failing navigator test**

`FakeOnboardingRepository.kt` (test sources; also used by later presenter tests):

```kotlin
package com.hedvig.android.feature.onboarding

import app.cash.turbine.Turbine
import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.onboarding.data.OnboardingContract
import com.hedvig.android.feature.onboarding.data.OnboardingCrossSell
import com.hedvig.android.feature.onboarding.data.OnboardingData
import com.hedvig.android.feature.onboarding.data.OnboardingReferralInformation
import com.hedvig.android.feature.onboarding.data.OnboardingRepository

internal class FakeOnboardingRepository : OnboardingRepository {
  val onboardingDataResponses = Turbine<Either<ErrorMessage, OnboardingData>>()
  val updateContactInfoResponses = Turbine<Either<ErrorMessage, Unit>>()

  override suspend fun getOnboardingData(): Either<ErrorMessage, OnboardingData> {
    return onboardingDataResponses.awaitItem()
  }

  override suspend fun updateContactInfo(email: String, phoneNumber: String): Either<ErrorMessage, Unit> {
    return updateContactInfoResponses.awaitItem()
  }
}

internal fun testOnboardingData(
  phoneNumber: String? = "070 990 12 32",
  contracts: List<OnboardingContract> = listOf(
    OnboardingContract(
      id = "contract-1",
      displayName = "Home Insurance",
      exposureName = "Bellmansgatan 19A",
      typeOfContract = "SE_APARTMENT_RENT",
      missingCoInsuredCount = 1,
      isMissingPetId = false,
    ),
  ),
  referralInformation: OnboardingReferralInformation? = OnboardingReferralInformation("CODE", 10.0, "SEK"),
  hasConnectedPayinMethod: Boolean = false,
  crossSells: List<OnboardingCrossSell> = listOf(OnboardingCrossSell("cs", "Pet", "For your pet", "https://x")),
): OnboardingData = OnboardingData(
  email = "member@example.com",
  phoneNumber = phoneNumber,
  contracts = contracts,
  referralInformation = referralInformation,
  hasConnectedPayinMethod = hasConnectedPayinMethod,
  crossSells = crossSells,
)
```

`OnboardingNavigatorTest.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.navigation

import androidx.compose.runtime.mutableStateListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.testOnboardingData
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class OnboardingNavigatorTest {
  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class FakeCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    var invoked: Boolean = false

    override suspend fun invoke() {
      invoked = true
    }
  }

  private suspend fun sessionStoreWithData(repository: FakeOnboardingRepository): OnboardingSessionStore {
    val store = OnboardingSessionStore(repository)
    repository.onboardingDataResponses.add(testOnboardingData().right())
    store.getOrFetchSession()
    return store
  }

  @Test
  fun `continue from welcome pushes the first step of the path`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingKey) }
    val repository = FakeOnboardingRepository()
    val navigator = OnboardingNavigator(backstack, sessionStoreWithData(repository), FakeCompleteOnboardingUseCase())

    navigator.continueFrom(null)

    assertThat(backstack.entries).containsExactly(
      OnboardingKey,
      OnboardingStepKey(OnboardingStepId.AnalyticsConsent),
    )
  }

  @Test
  fun `continue from a mid step pushes the next step`() = runTest {
    val backstack = TestBackstack().apply {
      entries.add(OnboardingKey)
      entries.add(OnboardingStepKey(OnboardingStepId.AnalyticsConsent))
    }
    val repository = FakeOnboardingRepository()
    val navigator = OnboardingNavigator(backstack, sessionStoreWithData(repository), FakeCompleteOnboardingUseCase())

    navigator.continueFrom(OnboardingStepId.AnalyticsConsent)

    assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
  }

  @Test
  fun `continue from the last step marks seen and removes all onboarding keys`() = runTest {
    val backstack = TestBackstack().apply {
      entries.add(OnboardingKey)
      entries.add(OnboardingStepKey(OnboardingStepId.BundleDiscount))
    }
    val repository = FakeOnboardingRepository()
    val completeOnboarding = FakeCompleteOnboardingUseCase()
    val navigator = OnboardingNavigator(backstack, sessionStoreWithData(repository), completeOnboarding)

    navigator.continueFrom(OnboardingStepId.BundleDiscount)

    assertThat(completeOnboarding.invoked).isTrue()
    assertThat(backstack.entries).isEmpty()
  }

  @Test
  fun `exit marks seen and removes all onboarding keys, leaving surrounding entries alone`() = runTest {
    val backstack = TestBackstack().apply {
      entries.add(NonOnboardingKey)
      entries.add(OnboardingKey)
      entries.add(OnboardingStepKey(OnboardingStepId.AnalyticsConsent))
    }
    val repository = FakeOnboardingRepository()
    val completeOnboarding = FakeCompleteOnboardingUseCase()
    val navigator = OnboardingNavigator(backstack, sessionStoreWithData(repository), completeOnboarding)

    navigator.exitOnboarding()

    assertThat(completeOnboarding.invoked).isTrue()
    assertThat(backstack.entries).containsExactly(NonOnboardingKey)
  }

  @Test
  fun `continue without a session exits the flow without marking crash-level state`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingKey) }
    val completeOnboarding = FakeCompleteOnboardingUseCase()
    val navigator = OnboardingNavigator(backstack, OnboardingSessionStore(FakeOnboardingRepository()), completeOnboarding)

    navigator.continueFrom(null)

    assertThat(backstack.entries).isEmpty()
    assertThat(completeOnboarding.invoked).isTrue()
  }
}

@kotlinx.serialization.Serializable
private data object NonOnboardingKey : HedvigNavKey
```

(Imports used above include `assertk.assertions.isEqualTo`; adjust the import list to what the file actually references.)

- [ ] **Step 4: Run to verify failure**

Run: `./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD|error:)" | head -5`
Expected: compile failure, `OnboardingNavigator` unresolved.

- [ ] **Step 5: Implement the navigator**

`OnboardingNavigator.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.navigation

import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.removeAllOf
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Owns forward/exit movement through the onboarding flow. Steps are pushed as real back stack
 * entries so predictive back and the up arrow behave natively; exiting removes every onboarding
 * key, landing on whatever was underneath (Home).
 */
@SingleIn(ActivityRetainedScope::class)
@Inject
internal class OnboardingNavigator(
  private val backstack: Backstack,
  private val sessionStore: OnboardingSessionStore,
  private val completeOnboardingUseCase: CompleteOnboardingUseCase,
) {
  /** [current] is null when continuing from the welcome screen (OnboardingKey). */
  suspend fun continueFrom(current: OnboardingStepId?) {
    val session = sessionStore.currentSession
    if (session == null) {
      logcat { "Onboarding continue without a session, exiting flow" }
      exitOnboarding()
      return
    }
    val next = if (current == null) {
      session.path.firstOrNull()
    } else {
      val currentIndex = session.path.indexOf(current)
      if (currentIndex == -1) null else session.path.getOrNull(currentIndex + 1)
    }
    if (next == null) {
      exitOnboarding()
    } else {
      backstack.add(OnboardingStepKey(next))
    }
  }

  suspend fun exitOnboarding() {
    completeOnboardingUseCase.invoke()
    backstack.removeAllOf<OnboardingStepKey>()
    backstack.removeAllOf<OnboardingKey>()
  }

  /** Reserved for the top app bar back arrow only, per the navigation rule. */
  fun navigateBack() {
    backstack.navigateUp()
  }
}
```

- [ ] **Step 6: Run tests, format, commit**

```bash
./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding session store, completion use case and navigator"
```

---

### Task 9: OnboardingStepScaffold (progress bar + back + close chrome)

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/OnboardingStepScaffold.kt`

**Interfaces:**
- Consumes: `OnboardingSession` (Task 8), design-system components.
- Produces (every step destination in Tasks 10-18 uses these exact signatures):
  - `internal data class OnboardingProgress(val totalSteps: Int, val currentIndex: Int)`
  - `internal fun OnboardingSession.progressFor(stepId: OnboardingStepId?): OnboardingProgress` (null = welcome at index 0; total is `path.size + 1`)
  - `internal fun OnboardingStepScaffold(progress: OnboardingProgress?, showBackButton: Boolean, onBackClick: () -> Unit, onCloseClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit)` (progress bar hidden while `progress == null`, e.g. during a restore-refetch)
  - `internal fun OnboardingStepHeader(title: String, description: String, modifier: Modifier = Modifier)`
  - `internal fun ColumnScope.OnboardingStepButtons(primaryText: String, onPrimaryClick: () -> Unit, primaryEnabled: Boolean = true, secondaryText: String? = null, onSecondaryClick: (() -> Unit)? = null)`

- [ ] **Step 1: Pin exact design-system API names**

Before writing code, read these files and note the exact names to use (the code below uses the most likely names; correct them to what you find):

- `app/design-system/design-system-hedvig/src/commonMain/kotlin/com/hedvig/android/design/system/hedvig/Icon.kt` (or grep `grep -rn "HedvigIcons.Close" app/feature --include=*.kt | head -3` and copy that call site's `Icon` import and usage shape)
- `app/design-system/design-system-hedvig/src/commonMain/kotlin/com/hedvig/android/design/system/hedvig/HedvigText.kt` for the text composable and how `style`/`color` are passed
- `app/design-system/design-system-hedvig/src/commonMain/kotlin/com/hedvig/android/design/system/hedvig/HedvigTheme.kt` for `colorScheme` token names (the code below assumes `fillPrimary` for active segments, `surfaceSecondary` for inactive, `textSecondary` for description text; substitute the real closest tokens)
- `Button.kt` for `HedvigButton` and `HedvigTextButton` exact parameter lists

- [ ] **Step 2: Implement**

`OnboardingStepScaffold.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowLeft
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.onboarding.data.OnboardingSession
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId

internal data class OnboardingProgress(
  val totalSteps: Int,
  val currentIndex: Int,
)

/**
 * Welcome (stepId == null) sits at progress position 0; path steps follow it. If [stepId] is not
 * in the path (possible only when a process-death refetch rebuilt a different path under a
 * restored step), this degrades to index 0 rather than crashing; the step stays fully usable.
 */
internal fun OnboardingSession.progressFor(stepId: OnboardingStepId?): OnboardingProgress {
  return OnboardingProgress(
    totalSteps = path.size + 1,
    currentIndex = if (stepId == null) 0 else path.indexOf(stepId) + 1,
  )
}

@Composable
internal fun OnboardingStepScaffold(
  progress: OnboardingProgress?,
  showBackButton: Boolean,
  onBackClick: () -> Unit,
  onCloseClick: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(modifier = modifier.fillMaxSize()) {
    Column(
      Modifier.windowInsetsPadding(
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
      ),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .padding(horizontal = 16.dp),
      ) {
        if (showBackButton) {
          Icon(
            imageVector = HedvigIcons.ArrowLeft,
            // TODO: Add "Go back" / "Gå tillbaka" to Lokalise (or reuse an existing a11y string)
            contentDescription = "Go back",
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .clickable(onClick = onBackClick),
          )
        } else {
          Spacer(Modifier.size(24.dp))
        }
        if (progress != null) {
          OnboardingProgressBar(
            progress = progress,
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 24.dp),
          )
        } else {
          Spacer(Modifier.weight(1f))
        }
        Icon(
          imageVector = HedvigIcons.Close,
          // TODO: Add "Close" / "Stäng" to Lokalise (or reuse an existing a11y string)
          contentDescription = "Close",
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .clickable(onClick = onCloseClick),
        )
      }
      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState()),
        content = content,
      )
    }
  }
}

@Composable
private fun OnboardingProgressBar(progress: OnboardingProgress, modifier: Modifier = Modifier) {
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
    repeat(progress.totalSteps) { index ->
      val isActivated = index <= progress.currentIndex
      Box(
        Modifier
          .weight(1f)
          .height(2.dp)
          .clip(CircleShape)
          .background(
            if (isActivated) HedvigTheme.colorScheme.fillPrimary else HedvigTheme.colorScheme.surfaceSecondary,
          ),
      )
    }
  }
}

@Composable
internal fun OnboardingStepHeader(title: String, description: String, modifier: Modifier = Modifier) {
  Column(modifier.padding(horizontal = 16.dp)) {
    HedvigText(text = title)
    HedvigText(text = description, color = HedvigTheme.colorScheme.textSecondary)
  }
}

/** Bottom-anchored button area: primary on top, optional ghost secondary below, matching Figma. */
@Composable
internal fun ColumnScope.OnboardingStepButtons(
  primaryText: String,
  onPrimaryClick: () -> Unit,
  primaryEnabled: Boolean = true,
  secondaryText: String? = null,
  onSecondaryClick: (() -> Unit)? = null,
) {
  Spacer(Modifier.weight(1f))
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = primaryText,
    onClick = onPrimaryClick,
    enabled = primaryEnabled,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  if (secondaryText != null && onSecondaryClick != null) {
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = secondaryText,
      onClick = onSecondaryClick,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
  }
  Spacer(Modifier.height(16.dp))
  Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
}
```

Adjust to the real API names found in Step 1 (`Icon` parameter names, `HedvigTextButton(onClick=...)` parameter order, color tokens, whether `Surface` is the right root or plain `Box(Modifier.background(...))` matches other full-screen destinations). Icon vectors: verify `HedvigIcons.ArrowLeft` and `HedvigIcons.Close` exist in `design-system-hedvig/.../icon/`; if `ArrowLeft` is missing, grep for the icon used by `TopAppBarActionType.BACK` and use that.

- [ ] **Step 3: Compile, format, commit**

```bash
./gradlew :feature-onboarding:compileDebugKotlin 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED|error:)" | head -10
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding step scaffold with segmented progress bar"
```

---

### Task 10: Welcome step

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/welcome/OnboardingWelcomeDestination.kt`
- Test: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/ui/welcome/OnboardingWelcomePresenterTest.kt`

**Interfaces:**
- Consumes: `OnboardingSessionStore.getOrFetchSession()`, `OnboardingNavigator.continueFrom(null)` / `exitOnboarding()`, `OnboardingStepScaffold`/`progressFor`/`OnboardingStepButtons` (Task 9), `metroViewModel` pattern.
- Produces: `OnboardingWelcomeViewModel` (`@Inject @HedvigViewModel(ActivityRetainedScope::class)`), `OnboardingWelcomeDestination(viewModel)`. Task 19 registers `entry<OnboardingKey>` with these.

Figma (node 597:1192): app-icon graphic with red "1" badge, title "Welcome to Hedvig", subtitle "Follow the steps to get started with your new insurance", primary button "Get started". No back arrow. The badge/checkmark animations noted in Figma are out of scope for v1 (tracked in the final task's follow-ups list).

- [ ] **Step 1: Write the failing presenter test**

`OnboardingWelcomePresenterTest.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.ui.welcome

import androidx.compose.runtime.mutableStateListOf
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingKey
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepKey
import com.hedvig.android.feature.onboarding.testOnboardingData
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class OnboardingWelcomePresenterTest {
  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class NoopCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    override suspend fun invoke() {}
  }

  @Test
  fun `session fetch success shows content with the progress of the whole path`() = runTest {
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository)
    val navigator = OnboardingNavigator(TestBackstack(), sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingWelcomePresenter(sessionStore, navigator)

    presenter.test(OnboardingWelcomeUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      val content = awaitItem()
      assertThat(content).isInstanceOf<OnboardingWelcomeUiState.Content>()
      // default test data: consent+phone+theme+coinsured+invite+payment+bundle = 7 steps + welcome
      assertThat((content as OnboardingWelcomeUiState.Content).progress.totalSteps).isEqualTo(8)
      assertThat(content.progress.currentIndex).isEqualTo(0)
    }
  }

  @Test
  fun `session fetch failure shows error, retry refetches`() = runTest {
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository)
    val navigator = OnboardingNavigator(TestBackstack(), sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingWelcomePresenter(sessionStore, navigator)

    presenter.test(OnboardingWelcomeUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(ErrorMessage("boom").left())
      assertThat(awaitItem()).isInstanceOf<OnboardingWelcomeUiState.Error>()
      sendEvent(OnboardingWelcomeEvent.Retry)
      skipItems(1) // back to Loading
      repository.onboardingDataResponses.add(testOnboardingData().right())
      assertThat(awaitItem()).isInstanceOf<OnboardingWelcomeUiState.Content>()
    }
  }

  @Test
  fun `get started pushes the first path step onto the backstack`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingKey) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository)
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingWelcomePresenter(sessionStore, navigator)

    presenter.test(OnboardingWelcomeUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingWelcomeEvent.GetStarted)
      awaitUnchanged()
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.AnalyticsConsent))
    }
  }
}
```

(If `awaitUnchanged()` is not the right way to let the launched event settle in `molecule-test`, mirror what an existing test in the repo does after `sendEvent` with a side effect, e.g. `runCurrent()` via the test scheduler.)

- [ ] **Step 2: Run to verify failure**

Run: `./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD|error:)" | head -5`
Expected: compile failure.

- [ ] **Step 3: Implement ViewModel, Presenter, Destination**

`OnboardingWelcomeDestination.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingStepButtons
import com.hedvig.android.feature.onboarding.ui.OnboardingStepScaffold
import com.hedvig.android.feature.onboarding.ui.progressFor
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingWelcomeViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
) : MoleculeViewModel<OnboardingWelcomeEvent, OnboardingWelcomeUiState>(
    initialState = OnboardingWelcomeUiState.Loading,
    presenter = OnboardingWelcomePresenter(sessionStore, navigator),
  )

internal class OnboardingWelcomePresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
) : MoleculePresenter<OnboardingWelcomeEvent, OnboardingWelcomeUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingWelcomeEvent>.present(
    lastState: OnboardingWelcomeUiState,
  ): OnboardingWelcomeUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingWelcomeUiState.Content) return@LaunchedEffect
      currentState = OnboardingWelcomeUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingWelcomeUiState.Error },
        ifRight = { session ->
          currentState = OnboardingWelcomeUiState.Content(session.progressFor(null))
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingWelcomeEvent.Retry -> loadIteration++
        OnboardingWelcomeEvent.GetStarted -> launch { navigator.continueFrom(null) }
        OnboardingWelcomeEvent.Close -> launch { navigator.exitOnboarding() }
      }
    }

    return currentState
  }
}

internal sealed interface OnboardingWelcomeUiState {
  data object Loading : OnboardingWelcomeUiState

  data object Error : OnboardingWelcomeUiState

  data class Content(val progress: OnboardingProgress) : OnboardingWelcomeUiState
}

internal sealed interface OnboardingWelcomeEvent {
  data object Retry : OnboardingWelcomeEvent

  data object GetStarted : OnboardingWelcomeEvent

  data object Close : OnboardingWelcomeEvent
}

@Composable
internal fun OnboardingWelcomeDestination(viewModel: OnboardingWelcomeViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingWelcomeUiState.Content)?.progress,
    showBackButton = false,
    onBackClick = {},
    onCloseClick = { viewModel.emit(OnboardingWelcomeEvent.Close) },
  ) {
    when (uiState) {
      OnboardingWelcomeUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
      OnboardingWelcomeUiState.Error -> HedvigErrorSection(
        onButtonClick = { viewModel.emit(OnboardingWelcomeEvent.Retry) },
      )
      is OnboardingWelcomeUiState.Content -> {
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          // Static stand-in for the Figma app-icon-with-badge graphic; animation is a follow-up.
          Box(
            Modifier
              .size(96.dp)
              .background(HedvigTheme.colorScheme.fillPrimary, HedvigTheme.shapes.cornerLarge),
          )
          Spacer(Modifier.height(24.dp))
          // TODO: Add "Welcome to Hedvig" / "Välkommen till Hedvig" to Lokalise
          HedvigText("Welcome to Hedvig")
          // TODO: Add "Follow the steps to get started with your new insurance" /
          //  "Följ stegen för att komma igång med din nya försäkring" to Lokalise
          HedvigText(
            text = "Follow the steps to get started with your new insurance",
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
        // TODO: Add "Get started" / "Kom igång" to Lokalise
        OnboardingStepButtons(
          primaryText = "Get started",
          onPrimaryClick = { viewModel.emit(OnboardingWelcomeEvent.GetStarted) },
        )
      }
    }
  }
}
```

(`HedvigTheme.shapes.cornerLarge`: substitute the real shape token found in Task 9's discovery. `HedvigFullScreenCenterAlignedProgressDebounced` is the loading composable the Settings screen uses; same import.)

- [ ] **Step 4: Run tests, format, commit**

```bash
./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding welcome step"
```

Shared conventions for Tasks 11-18 (each step task repeats the welcome shape; only differences are spelled out per task):

- Every step has `{Step}ViewModel` (`@Inject @HedvigViewModel(ActivityRetainedScope::class)`), `{Step}Presenter`, `{Step}UiState` (`Loading`/`Error`/`Content`), `{Step}Event` (always includes `Retry` and `Close`), and `{Step}Destination(viewModel, ...)` in one file, exactly like Task 10's welcome file.
- Every presenter loads the session with the same `LaunchedEffect(loadIteration)` block as `OnboardingWelcomePresenter`, computing `session.progressFor(OnboardingStepId.X)` for its own step id.
- `Close` always does `launch { navigator.exitOnboarding() }`; the step's forward action always ends in `launch { navigator.continueFrom(OnboardingStepId.X) }`.
- Every destination renders inside `OnboardingStepScaffold(progress, showBackButton = true, onBackClick = { navigator-back callback }, onCloseClick = { emit Close })`, with `Loading -> HedvigFullScreenCenterAlignedProgressDebounced()`, `Error -> HedvigErrorSection(onButtonClick = { emit Retry })`. The back callback is threaded from the entries function as `navigateUp: () -> Unit` (Task 19 wires it to `backstack::navigateUp`).
- Steps that return from an external flow (co-insured, pet ids, payment) add this refresh block to their destination and a `Refresh` event handled as `launch { sessionStore.refreshData() /* onRight updates content, onLeft keeps previous */ }`:

```kotlin
var isFirstResume by rememberSaveable { mutableStateOf(true) }
LifecycleResumeEffect(Unit) {
  if (isFirstResume) isFirstResume = false else viewModel.emit(/* Step */Event.Refresh)
  onPauseOrDispose {}
}
```

- Presenter tests follow `OnboardingWelcomePresenterTest`: real `OnboardingSessionStore` + `FakeOnboardingRepository` + real `OnboardingNavigator` over a local `TestBackstack`, `presenter.test(initialState) { ... }`.

---

### Task 11: Analytics consent step

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/consent/OnboardingConsentDestination.kt`
- Test: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/ui/consent/OnboardingConsentPresenterTest.kt`

**Interfaces:**
- Consumes: shared conventions, `SettingsDataStore.setAnalyticsConsent(...)` (Task 1).
- Produces: `OnboardingConsentViewModel(sessionStore, navigator, settingsDataStore)`, `OnboardingConsentDestination(viewModel, navigateUp: () -> Unit, openPrivacyPolicy: () -> Unit)`.

Figma (597:1211 area): title "Help us make the app better", body "We use technical tools to see how you use the app, so we can make it better.\n\nProduct analytics is completely optional and can be turned off any time in settings. This data is never used for marketing.", "Privacy policy" link, buttons "Allow" (primary) / "Deny" (ghost). Both choices advance.

- [ ] **Step 1: Write the failing presenter test**

Key cases (full file, following the welcome test's construction pattern; `FakeSettingsDataStore` reuses Task 2's fake, move it to `src/test/kotlin/com/hedvig/android/feature/onboarding/FakeSettingsDataStore.kt` in THIS module as its own copy since test fixtures don't cross modules):

```kotlin
package com.hedvig.android.feature.onboarding.ui.consent

import androidx.compose.runtime.mutableStateListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.FakeSettingsDataStore
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepKey
import com.hedvig.android.feature.onboarding.testOnboardingData
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class OnboardingConsentPresenterTest {
  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class NoopCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    override suspend fun invoke() {}
  }

  @Test
  fun `allow stores GRANTED and advances to the next step`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.AnalyticsConsent)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository)
    val settingsDataStore = FakeSettingsDataStore()
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingConsentPresenter(sessionStore, navigator, settingsDataStore)

    presenter.test(OnboardingConsentUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      assertThat(awaitItem()).isInstanceOf<OnboardingConsentUiState.Content>()
      sendEvent(OnboardingConsentEvent.Allow)
      awaitUnchanged()
      assertThat(settingsDataStore.consent.value).isEqualTo(AnalyticsConsent.GRANTED)
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
    }
  }

  @Test
  fun `deny stores DENIED and still advances`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.AnalyticsConsent)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository)
    val settingsDataStore = FakeSettingsDataStore()
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingConsentPresenter(sessionStore, navigator, settingsDataStore)

    presenter.test(OnboardingConsentUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingConsentEvent.Deny)
      awaitUnchanged()
      assertThat(settingsDataStore.consent.value).isEqualTo(AnalyticsConsent.DENIED)
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
    }
  }
}
```

- [ ] **Step 2: Run to verify failure** (same command as Task 10 Step 2)

- [ ] **Step 3: Implement**

`OnboardingConsentDestination.kt` (ViewModel + Presenter + state/events + Destination in one file, welcome-file shape). The differing parts:

```kotlin
@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingConsentViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  settingsDataStore: SettingsDataStore,
) : MoleculeViewModel<OnboardingConsentEvent, OnboardingConsentUiState>(
    initialState = OnboardingConsentUiState.Loading,
    presenter = OnboardingConsentPresenter(sessionStore, navigator, settingsDataStore),
  )

internal class OnboardingConsentPresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
  private val settingsDataStore: SettingsDataStore,
) : MoleculePresenter<OnboardingConsentEvent, OnboardingConsentUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingConsentEvent>.present(
    lastState: OnboardingConsentUiState,
  ): OnboardingConsentUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingConsentUiState.Content) return@LaunchedEffect
      currentState = OnboardingConsentUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingConsentUiState.Error },
        ifRight = { session ->
          currentState = OnboardingConsentUiState.Content(session.progressFor(OnboardingStepId.AnalyticsConsent))
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingConsentEvent.Retry -> loadIteration++
        OnboardingConsentEvent.Close -> launch { navigator.exitOnboarding() }
        OnboardingConsentEvent.Allow -> launch {
          settingsDataStore.setAnalyticsConsent(AnalyticsConsent.GRANTED)
          navigator.continueFrom(OnboardingStepId.AnalyticsConsent)
        }
        OnboardingConsentEvent.Deny -> launch {
          settingsDataStore.setAnalyticsConsent(AnalyticsConsent.DENIED)
          navigator.continueFrom(OnboardingStepId.AnalyticsConsent)
        }
      }
    }

    return currentState
  }
}

internal sealed interface OnboardingConsentUiState {
  data object Loading : OnboardingConsentUiState

  data object Error : OnboardingConsentUiState

  data class Content(val progress: OnboardingProgress) : OnboardingConsentUiState
}

internal sealed interface OnboardingConsentEvent {
  data object Retry : OnboardingConsentEvent

  data object Close : OnboardingConsentEvent

  data object Allow : OnboardingConsentEvent

  data object Deny : OnboardingConsentEvent
}
```

Destination content (inside the shared scaffold; header at the top, buttons at the bottom):

```kotlin
@Composable
internal fun OnboardingConsentDestination(
  viewModel: OnboardingConsentViewModel,
  navigateUp: () -> Unit,
  openPrivacyPolicy: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingConsentUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = { viewModel.emit(OnboardingConsentEvent.Close) },
  ) {
    when (uiState) {
      OnboardingConsentUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
      OnboardingConsentUiState.Error -> HedvigErrorSection(
        onButtonClick = { viewModel.emit(OnboardingConsentEvent.Retry) },
      )
      is OnboardingConsentUiState.Content -> {
        Spacer(Modifier.height(8.dp))
        OnboardingStepHeader(
          // TODO: Add "Help us make the app better" / "Hjälp oss göra appen bättre" to Lokalise
          title = "Help us make the app better",
          // TODO: Add the body copy below (and its Swedish translation) to Lokalise
          description = "We use technical tools to see how you use the app, so we can make it better.\n\n" +
            "Product analytics is completely optional and can be turned off any time in settings. " +
            "This data is never used for marketing.",
        )
        Spacer(Modifier.weight(1f))
        // TODO: Add "Privacy policy" / "Integritetspolicy" to Lokalise
        HedvigTextButton(
          text = "Privacy policy",
          onClick = openPrivacyPolicy,
          modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        OnboardingStepButtons(
          // TODO: Add "Allow" / "Tillåt" to Lokalise
          primaryText = "Allow",
          onPrimaryClick = { viewModel.emit(OnboardingConsentEvent.Allow) },
          // TODO: Add "Deny" / "Neka" to Lokalise
          secondaryText = "Deny",
          onSecondaryClick = { viewModel.emit(OnboardingConsentEvent.Deny) },
        )
      }
    }
  }
}
```

`openPrivacyPolicy` is a `() -> Unit` threaded from `:app` in Task 19 (it calls `openUrl` with the privacy policy URL that `:app` already knows; grep `:app` and `feature-login` for an existing privacy-policy URL constant and reuse it there, keeping this module URL-free).

- [ ] **Step 4: Run tests, format, commit**

```bash
./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding analytics consent step"
```

---

### Task 12: Phone number step

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/phone/OnboardingPhoneDestination.kt`
- Test: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/ui/phone/OnboardingPhonePresenterTest.kt`

**Interfaces:**
- Consumes: shared conventions, `OnboardingRepository.updateContactInfo(email, phoneNumber)` (Task 5; email comes from the session's `OnboardingData.email`, resubmitted unchanged since the mutation requires both fields).
- Produces: `OnboardingPhoneViewModel(sessionStore, navigator, onboardingRepository)`, `OnboardingPhoneDestination(viewModel, navigateUp)`.

Figma: title "Phone number", body "Add your phone number so we can reach you if something happens", text field labeled "Phone number" pre-filled with the current number, buttons "Save" (primary) / "Do this later" (ghost). Save failure shows an inline field error and stays.

- [ ] **Step 1: Write the failing presenter test**

Cases (full file following Task 11's construction pattern):

1. `content pre-fills the member's phone number`: fetch session with `phoneNumber = "070 990 12 32"`, assert `Content.phoneNumber == "070 990 12 32"`.
2. `save success advances to the next step`: `sendEvent(UpdatePhoneNumber("0701234567"))`, `sendEvent(Save)`, respond `repository.updateContactInfoResponses.add(Unit.right())`, assert backstack last == `OnboardingStepKey(OnboardingStepId.Theme)`.
3. `save failure shows inline error and does not advance`: respond `add(ErrorMessage("nope").left())`, assert `Content.showSubmissionError` is true and backstack unchanged.
4. `do this later advances without calling the mutation`: `sendEvent(DoThisLater)`, assert backstack advanced and `repository.updateContactInfoResponses` received no request (Turbine: `expectNoEvents()` on the request side is not available on a plain `Turbine` used as a response queue, so instead track calls with a `var updateContactInfoCallCount` added to `FakeOnboardingRepository` and assert it is 0).

Add to `FakeOnboardingRepository` (Task 8 fixture): `var updateContactInfoCallCount: Int = 0` incremented at the top of `updateContactInfo`.

- [ ] **Step 2: Run to verify failure** (same command)

- [ ] **Step 3: Implement**

State/events and presenter (welcome-file shape; destination below):

```kotlin
internal sealed interface OnboardingPhoneUiState {
  data object Loading : OnboardingPhoneUiState

  data object Error : OnboardingPhoneUiState

  data class Content(
    val progress: OnboardingProgress,
    val phoneNumber: String,
    val isSubmitting: Boolean = false,
    val showSubmissionError: Boolean = false,
  ) : OnboardingPhoneUiState
}

internal sealed interface OnboardingPhoneEvent {
  data object Retry : OnboardingPhoneEvent

  data object Close : OnboardingPhoneEvent

  data class UpdatePhoneNumber(val phoneNumber: String) : OnboardingPhoneEvent

  data object Save : OnboardingPhoneEvent

  data object DoThisLater : OnboardingPhoneEvent
}

internal class OnboardingPhonePresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
  private val onboardingRepository: OnboardingRepository,
) : MoleculePresenter<OnboardingPhoneEvent, OnboardingPhoneUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingPhoneEvent>.present(
    lastState: OnboardingPhoneUiState,
  ): OnboardingPhoneUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingPhoneUiState.Content) return@LaunchedEffect
      currentState = OnboardingPhoneUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingPhoneUiState.Error },
        ifRight = { session ->
          currentState = OnboardingPhoneUiState.Content(
            progress = session.progressFor(OnboardingStepId.PhoneNumber),
            phoneNumber = session.data.phoneNumber.orEmpty(),
          )
        },
      )
    }

    LaunchedEffect(submitIteration) {
      if (submitIteration == 0) return@LaunchedEffect
      val content = currentState as? OnboardingPhoneUiState.Content ?: return@LaunchedEffect
      val session = sessionStore.currentSession ?: return@LaunchedEffect
      currentState = content.copy(isSubmitting = true, showSubmissionError = false)
      onboardingRepository.updateContactInfo(
        email = session.data.email,
        phoneNumber = content.phoneNumber,
      ).fold(
        ifLeft = {
          currentState = content.copy(isSubmitting = false, showSubmissionError = true)
        },
        ifRight = {
          navigator.continueFrom(OnboardingStepId.PhoneNumber)
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingPhoneEvent.Retry -> loadIteration++
        OnboardingPhoneEvent.Close -> launch { navigator.exitOnboarding() }
        is OnboardingPhoneEvent.UpdatePhoneNumber -> {
          val content = currentState as? OnboardingPhoneUiState.Content ?: return@CollectEvents
          currentState = content.copy(phoneNumber = event.phoneNumber, showSubmissionError = false)
        }
        OnboardingPhoneEvent.Save -> submitIteration++
        OnboardingPhoneEvent.DoThisLater -> launch { navigator.continueFrom(OnboardingStepId.PhoneNumber) }
      }
    }

    return currentState
  }
}
```

Destination content for the `Content` branch:

```kotlin
        Spacer(Modifier.height(8.dp))
        OnboardingStepHeader(
          // TODO: Add "Phone number" / "Telefonnummer" to Lokalise
          title = "Phone number",
          // TODO: Add "Add your phone number so we can reach you if something happens" /
          //  "Lägg till ditt telefonnummer så att vi kan nå dig om något händer" to Lokalise
          description = "Add your phone number so we can reach you if something happens",
        )
        Spacer(Modifier.height(16.dp))
        HedvigTextField(
          text = content.phoneNumber,
          onValueChange = { viewModel.emit(OnboardingPhoneEvent.UpdatePhoneNumber(it)) },
          // TODO: Add "Phone number" / "Telefonnummer" to Lokalise
          labelText = "Phone number",
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          errorState = if (content.showSubmissionError) {
            // TODO: Add "Could not save, please try again" / "Kunde inte spara, försök igen" to Lokalise
            HedvigTextFieldDefaults.ErrorState.Error.WithMessage("Could not save, please try again")
          } else {
            HedvigTextFieldDefaults.ErrorState.NoError
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        OnboardingStepButtons(
          // TODO: Add "Save" / "Spara" to Lokalise
          primaryText = "Save",
          onPrimaryClick = { viewModel.emit(OnboardingPhoneEvent.Save) },
          primaryEnabled = !content.isSubmitting && content.phoneNumber.isNotBlank(),
          // TODO: Add "Do this later" / "Gör det senare" to Lokalise
          secondaryText = "Do this later",
          onSecondaryClick = { viewModel.emit(OnboardingPhoneEvent.DoThisLater) },
        )
```

(Verify `HedvigTextField`'s exact `errorState`/`keyboardOptions` parameter names against `HedvigTextField.kt`; the signature confirmed in research is `HedvigTextField(text, onValueChange, labelText, textFieldSize, modifier, ..., errorState, ...)`.)

- [ ] **Step 4: Run tests, format, commit**

```bash
./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding phone number step"
```

---

### Task 13: Theme step

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/theme/OnboardingThemeDestination.kt`
- Test: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/ui/theme/OnboardingThemePresenterTest.kt`

**Interfaces:**
- Consumes: shared conventions, `SettingsDataStore.setTheme(theme)` / `observeTheme(): Flow<Theme?>`, `Theme` enum (`com.hedvig.android.theme`: `LIGHT, DARK, SYSTEM_DEFAULT`).
- Produces: `OnboardingThemeViewModel(sessionStore, navigator, settingsDataStore)`, `OnboardingThemeDestination(viewModel, navigateUp)`.

Figma: title "Choose theme", body "Customize the look of the app", three options (System "Uses your phone's setting", Light "Set light mode", Dark "Set dark mode"), hint "You can change these settings later", single primary "Continue". Selection applies immediately (the app-wide theme observes `SettingsDataStore` already, via `HedvigAppState.darkTheme`).

- [ ] **Step 1: Write the failing presenter test**

Full file following Task 11's construction pattern (TestBackstack + FakeOnboardingRepository + FakeSettingsDataStore + real session store/navigator). `FakeSettingsDataStore` needs a working theme: back `setTheme`/`observeTheme` with a `MutableStateFlow<Theme?>(null)` instead of `error("unused")`. Cases:

1. `selected theme defaults to SYSTEM_DEFAULT when nothing stored`: fetch session, assert `Content.selectedTheme == Theme.SYSTEM_DEFAULT`.
2. `selecting a theme persists it and updates the selection`: `sendEvent(SelectTheme(Theme.DARK))`, then assert the fake's theme flow value is `Theme.DARK` and the next emitted `Content.selectedTheme == Theme.DARK`.
3. `continue advances to the next path step`: `sendEvent(Continue)`, `awaitUnchanged()`, assert `backstack.entries.last()` equals the step after Theme for the default test data (`OnboardingStepKey(OnboardingStepId.CoInsured)`).

- [ ] **Step 2: Run to verify failure** (same command)

- [ ] **Step 3: Implement**

State/events and the presenter's differing parts (rest follows the welcome-file shape):

```kotlin
internal sealed interface OnboardingThemeUiState {
  data object Loading : OnboardingThemeUiState

  data object Error : OnboardingThemeUiState

  data class Content(
    val progress: OnboardingProgress,
    val selectedTheme: Theme,
  ) : OnboardingThemeUiState
}

internal sealed interface OnboardingThemeEvent {
  data object Retry : OnboardingThemeEvent

  data object Close : OnboardingThemeEvent

  data class SelectTheme(val theme: Theme) : OnboardingThemeEvent

  data object Continue : OnboardingThemeEvent
}
```

Presenter specifics: alongside the shared session `LaunchedEffect`, observe the stored theme and merge it into content:

```kotlin
    val storedTheme = settingsDataStore.observeTheme().collectAsState(null).value
    // In the return: if currentState is Content, return it with
    // selectedTheme = storedTheme ?: Theme.SYSTEM_DEFAULT
```

`CollectEvents` additions:

```kotlin
        is OnboardingThemeEvent.SelectTheme -> launch { settingsDataStore.setTheme(event.theme) }
        OnboardingThemeEvent.Continue -> launch { navigator.continueFrom(OnboardingStepId.Theme) }
```

Destination `Content` branch: header, then three option rows, hint, and the button:

```kotlin
@Composable
private fun ThemeOptionRow(
  title: String,
  subtitle: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .clip(HedvigTheme.shapes.cornerMedium)
      .clickable(onClick = onClick)
      .padding(16.dp),
  ) {
    Column(Modifier.weight(1f)) {
      HedvigText(title)
      HedvigText(subtitle, color = HedvigTheme.colorScheme.textSecondary)
    }
    if (isSelected) {
      Icon(imageVector = HedvigIcons.Checkmark, contentDescription = null)
    }
  }
}
```

Rows (all copy gets `// TODO: Add ... to Lokalise` comments; the theme display names may already exist as string resources used by the profile settings theme dialog, grep `SETTINGS_THEME` in `feature-profile` first and reuse `Res.string` ids if present):

- "System" / "Uses your phone's setting" -> `SelectTheme(Theme.SYSTEM_DEFAULT)`
- "Light" / "Set light mode" -> `SelectTheme(Theme.LIGHT)`
- "Dark" / "Set dark mode" -> `SelectTheme(Theme.DARK)`

Then the hint ("You can change these settings later", `textSecondary`, centered) directly above `OnboardingStepButtons(primaryText = "Continue", onPrimaryClick = { viewModel.emit(OnboardingThemeEvent.Continue) })`.

- [ ] **Step 4: Run tests, format, commit**

```bash
./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding theme step"
```

---

### Task 14: Add co-insured step

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/coinsured/OnboardingCoInsuredDestination.kt`
- Modify: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/navigation/OnboardingNavigator.kt`
- Test: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/ui/coinsured/OnboardingCoInsuredPresenterTest.kt`

**Interfaces:**
- Consumes: shared conventions (including the refresh-on-resume block), `CoInsuredAddInfoKey(contractId, type)` + `CoInsuredFlowType.CoInsured` from `feature-edit-coinsured-navigation` (a legal dependency).
- Produces: navigator gains `fun openAddCoInsured(contractId: String)`; `OnboardingCoInsuredViewModel(sessionStore, navigator)`, `OnboardingCoInsuredDestination(viewModel, navigateUp)`.

Figma: title "Add co-insured", body "So we know who's covered by your insurance", one row per contract that had missing co-insured info when the step loaded (contract display name + exposure, trailing "Add" button; a checkmark replaces the button once complete), hint "You can add this information later", buttons "Continue" / "Do this later". Rows are pinned at step load so a completed row shows its done state instead of vanishing (Figma's second variant).

- [ ] **Step 1: Add the navigator method**

In `OnboardingNavigator`:

```kotlin
  /** Pushes the existing edit-co-insured flow; it pops itself back here when done. */
  fun openAddCoInsured(contractId: String) {
    backstack.add(CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoInsured))
  }
```

(Imports: `com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddInfoKey`, `...CoInsuredFlowType`; verify exact package in `feature-edit-coinsured-navigation`.)

- [ ] **Step 2: Write the failing presenter test**

Cases (Task 11 construction pattern):

1. `rows are pinned from the contracts missing co-insured at load`: default test data has one contract with `missingCoInsuredCount = 1`; assert `Content.rows` has one row with `isComplete == false`.
2. `add navigates to the edit co-insured flow`: `sendEvent(AddCoInsured("contract-1"))`, `awaitUnchanged()`, assert `backstack.entries.last()` is `CoInsuredAddInfoKey("contract-1", CoInsuredFlowType.CoInsured)`.
3. `refresh marks completed rows instead of removing them`: `sendEvent(Refresh)`, respond with `testOnboardingData(contracts = listOf(<same contract but missingCoInsuredCount = 0>)).right()`, assert the single row now has `isComplete == true` (row count unchanged).
4. `continue advances`: assert next key is `OnboardingStepKey(OnboardingStepId.InviteFriend)` for default data (PetIds is skipped: default contract has `isMissingPetId = false`).

- [ ] **Step 3: Run to verify failure** (same command)

- [ ] **Step 4: Implement**

State/events:

```kotlin
internal data class CoInsuredRow(
  val contractId: String,
  val displayName: String,
  val exposureName: String,
  val isComplete: Boolean,
)

internal sealed interface OnboardingCoInsuredUiState {
  data object Loading : OnboardingCoInsuredUiState

  data object Error : OnboardingCoInsuredUiState

  data class Content(
    val progress: OnboardingProgress,
    val rows: List<CoInsuredRow>,
  ) : OnboardingCoInsuredUiState
}

internal sealed interface OnboardingCoInsuredEvent {
  data object Retry : OnboardingCoInsuredEvent

  data object Close : OnboardingCoInsuredEvent

  data object Refresh : OnboardingCoInsuredEvent

  data class AddCoInsured(val contractId: String) : OnboardingCoInsuredEvent

  data object Continue : OnboardingCoInsuredEvent
}
```

Presenter specifics beyond the shared shape:

```kotlin
    // Pin which contracts belong to this step the first time data is available, so completed
    // rows render as done instead of disappearing.
    var pinnedContractIds by remember { mutableStateOf<List<String>?>(null) }

    fun contentFrom(session: OnboardingSession): OnboardingCoInsuredUiState.Content {
      val ids = pinnedContractIds
        ?: session.data.contractsWithMissingCoInsured.map { it.id }.also { pinnedContractIds = it }
      return OnboardingCoInsuredUiState.Content(
        progress = session.progressFor(OnboardingStepId.CoInsured),
        rows = ids.mapNotNull { id ->
          val contract = session.data.contracts.firstOrNull { it.id == id } ?: return@mapNotNull null
          CoInsuredRow(
            contractId = contract.id,
            displayName = contract.displayName,
            exposureName = contract.exposureName,
            isComplete = contract.missingCoInsuredCount == 0,
          )
        },
      )
    }
```

The session `LaunchedEffect` uses `contentFrom(session)`; `CollectEvents` adds:

```kotlin
        OnboardingCoInsuredEvent.Refresh -> launch {
          sessionStore.refreshData().onRight { refreshed -> currentState = contentFrom(refreshed) }
          // onLeft: keep the previously shown state, per the spec's error handling.
        }
        is OnboardingCoInsuredEvent.AddCoInsured -> navigator.openAddCoInsured(event.contractId)
        OnboardingCoInsuredEvent.Continue -> launch { navigator.continueFrom(OnboardingStepId.CoInsured) }
```

Destination `Content` branch: header ("Add co-insured" / "So we know who's covered by your insurance", both with Lokalise TODOs), then per row:

```kotlin
        for (row in content.rows) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
          ) {
            Column(Modifier.weight(1f)) {
              HedvigText(row.displayName)
              HedvigText(row.exposureName, color = HedvigTheme.colorScheme.textSecondary)
            }
            if (row.isComplete) {
              Icon(imageVector = HedvigIcons.Checkmark, contentDescription = null)
            } else {
              HedvigButton(
                // TODO: Add "Add" / "Lägg till" to Lokalise
                text = "Add",
                onClick = { viewModel.emit(OnboardingCoInsuredEvent.AddCoInsured(row.contractId)) },
                enabled = true,
                buttonSize = ButtonDefaults.ButtonSize.Small,
              )
            }
          }
        }
```

Hint "You can add this information later" (Lokalise TODO) above `OnboardingStepButtons(primaryText = "Continue", ..., secondaryText = "Do this later", onSecondaryClick = { viewModel.emit(OnboardingCoInsuredEvent.Continue) })`. Note: both Continue and Do this later advance the same way here; emit `Continue` from both (the Figma distinction is visual only). Include the refresh-on-resume block from the shared conventions.

- [ ] **Step 5: Run tests, format, commit**

```bash
./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding co-insured step"
```

---

### Task 15: Pet ID numbers step

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/petid/OnboardingPetIdDestination.kt`
- Test: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/ui/petid/OnboardingPetIdPresenterTest.kt`

**Interfaces:**
- Consumes: shared conventions. NOT `ChipIdKey`: `feature-chip-id` is not a `-navigation` module, so the destination takes `onAddPetId: (contractId: String) -> Unit`, wired by `:app` in Task 19 to `backstack.add(ChipIdKey(contractId))`.
- Produces: `OnboardingPetIdViewModel(sessionStore, navigator)`, `OnboardingPetIdDestination(viewModel, navigateUp, onAddPetId)`.

Figma: title "Add your pets ID numbers", body "This makes it easier to help you if something happens", one pinned row per contract with `isMissingPetId` at load (pet contract display name + exposure name, "Add" button, checkmark when complete after refresh), hint "You can add this information later", Continue / Do this later.

This task mirrors the co-insured step, which by now exists in the repo. First read the committed `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/coinsured/OnboardingCoInsuredDestination.kt` and its test, then write this step's files as a full copy of that structure with these substitutions:

- Names: `PetIdRow`, `OnboardingPetIdUiState`, `OnboardingPetIdEvent` (`Retry, Close, Refresh, AddPetId(contractId), Continue`), `OnboardingPetIdPresenter`, `OnboardingPetIdViewModel`, `OnboardingPetIdDestination`.
- Pinning source: `session.data.contractsWithMissingPetId`; completion check: `contract.isMissingPetId == false`.
- `AddPetId` is NOT handled by the navigator; the presenter cannot open it. Route it through the destination instead: the `Content` row's Add button calls `onAddPetId(row.contractId)` directly, so the event enum drops `AddPetId` entirely and the presenter handles only `Retry, Close, Refresh, Continue`.
- Progress id: `OnboardingStepId.PetIds`; continue target asserts (test) with test data where the contract has `isMissingPetId = true` and `missingCoInsuredCount = 0`: next is `OnboardingStepKey(OnboardingStepId.InviteFriend)`.
- Copy with Lokalise TODOs: "Add your pets ID numbers" / "This makes it easier to help you if something happens" / "Add" / "You can add this information later" / "Continue" / "Do this later".

- [ ] **Step 1: Write the failing presenter test** (cases: pinned rows at load; refresh marks complete; continue advances)
- [ ] **Step 2: Run to verify failure** (same command)
- [ ] **Step 3: Implement destination file** (full welcome-file shape with the substitutions above)
- [ ] **Step 4: Run tests, format, commit**

```bash
./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding pet id step"
```

---

### Task 16: Invite a friend step

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/invite/OnboardingInviteDestination.kt`
- Test: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/ui/invite/OnboardingInvitePresenterTest.kt`

**Interfaces:**
- Consumes: shared conventions, `OnboardingReferralInformation` from the session.
- Produces: `OnboardingInviteViewModel(sessionStore, navigator)`, `OnboardingInviteDestination(viewModel, navigateUp)`.

Figma: title "Invite a friend", body "With Hedvig Forever, you get {incentive} off for every friend you invite", an illustration card of referral discounts (rendered as a simple static card listing "-{incentive}" rows is out of scope; render just the header and buttons plus one HedvigText showing the member's code), buttons "Invite a friend" (ghost, opens the system share sheet) / "Continue" (primary).

- [ ] **Step 1: Write the failing presenter test**

Cases:

1. `content carries the referral code and incentive`: default test data has `OnboardingReferralInformation("CODE", 10.0, "SEK")`; assert `Content.code == "CODE"` and `Content.incentiveDisplay == "10 SEK"`.
2. `continue advances`: next for default data is `OnboardingStepKey(OnboardingStepId.ConnectPayment)`.
3. `step renders error state if the session has no referral information` (defensive: gate/path should prevent this): fetch with `referralInformation = null`, assert `Error`.

- [ ] **Step 2: Run to verify failure** (same command)

- [ ] **Step 3: Implement**

State/events:

```kotlin
internal sealed interface OnboardingInviteUiState {
  data object Loading : OnboardingInviteUiState

  data object Error : OnboardingInviteUiState

  data class Content(
    val progress: OnboardingProgress,
    val code: String,
    val incentiveDisplay: String,
  ) : OnboardingInviteUiState
}

internal sealed interface OnboardingInviteEvent {
  data object Retry : OnboardingInviteEvent

  data object Close : OnboardingInviteEvent

  data object Continue : OnboardingInviteEvent
}
```

Presenter: session load maps `referralInformation` (null -> `Error`); `incentiveDisplay = "${referral.monthlyDiscountPerReferralAmount.toInt()} ${referral.currencyCode}"`. `Continue -> launch { navigator.continueFrom(OnboardingStepId.InviteFriend) }`.

Destination: the share action lives in the destination (needs `Context`), mirroring `ForeverDestination`'s share-sheet approach with a local helper:

```kotlin
private fun Context.showShareSheet(title: String, text: String) {
  startActivity(
    Intent.createChooser(
      Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
      },
      title,
    ),
  )
}
```

`Content` branch: header ("Invite a friend" / "With Hedvig Forever, you get {incentiveDisplay} off for every friend you invite", Lokalise TODOs), a centered `HedvigText(content.code)` as the code display, then:

```kotlin
        OnboardingStepButtons(
          // TODO: Add "Continue" / "Fortsätt" to Lokalise
          primaryText = "Continue",
          onPrimaryClick = { viewModel.emit(OnboardingInviteEvent.Continue) },
          // TODO: Add "Invite a friend" / "Bjud in en vän" to Lokalise
          secondaryText = "Invite a friend",
          onSecondaryClick = {
            // TODO: Add the share message to Lokalise; align copy with the Forever screen's
            //  REFERRAL_SMS_MESSAGE once product confirms.
            context.showShareSheet(
              title = "Invite a friend",
              text = "With my code you get a discount at Hedvig. Use code ${content.code}.",
            )
          },
        )
```

(`val context = LocalContext.current` at the destination top.)

- [ ] **Step 4: Run tests, format, commit**

```bash
./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding invite a friend step"
```

---

### Task 17: Connect payment step

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/payment/OnboardingPaymentDestination.kt`
- Modify: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/navigation/OnboardingNavigator.kt`
- Test: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/ui/payment/OnboardingPaymentPresenterTest.kt`

**Interfaces:**
- Consumes: shared conventions (with refresh-on-resume), `TrustlyKey` from `feature-connect-payment-trustly-navigation` (a legal dependency).
- Produces: navigator gains `fun openConnectPayment() { backstack.add(TrustlyKey) }`; `OnboardingPaymentViewModel(sessionStore, navigator)`, `OnboardingPaymentDestination(viewModel, navigateUp)`.

Figma, two variants: not connected shows title "Connect payment", body "Add a payment method to activate your insurance", caption "Adding a payment method is required to keep your insurance active", single primary "Connect payment" (no "Do this later"; the X remains the only skip). Connected (after returning from Trustly and refreshing) shows a checkmark graphic, hint "You can switch accounts later in settings", primary "Continue".

- [ ] **Step 1: Add the navigator method**

```kotlin
  /** Pushes the Trustly connect-payment flow; it pops itself back here when done. */
  fun openConnectPayment() {
    backstack.add(TrustlyKey)
  }
```

(Import `com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey`.)

- [ ] **Step 2: Write the failing presenter test**

Cases:

1. `not connected content when payin methods are missing`: default test data (`hasConnectedPayinMethod = false`), assert `Content.isConnected == false`.
2. `connect payment pushes the trustly flow`: `sendEvent(ConnectPayment)`, `awaitUnchanged()`, assert `backstack.entries.last()` is `TrustlyKey`.
3. `refresh after returning flips to connected`: `sendEvent(Refresh)`, respond `testOnboardingData(hasConnectedPayinMethod = true).right()`, assert `Content.isConnected == true`.
4. `continue advances`: for default data, next is `OnboardingStepKey(OnboardingStepId.BundleDiscount)`.

- [ ] **Step 3: Run to verify failure** (same command)

- [ ] **Step 4: Implement**

State/events:

```kotlin
internal sealed interface OnboardingPaymentUiState {
  data object Loading : OnboardingPaymentUiState

  data object Error : OnboardingPaymentUiState

  data class Content(
    val progress: OnboardingProgress,
    val isConnected: Boolean,
  ) : OnboardingPaymentUiState
}

internal sealed interface OnboardingPaymentEvent {
  data object Retry : OnboardingPaymentEvent

  data object Close : OnboardingPaymentEvent

  data object Refresh : OnboardingPaymentEvent

  data object ConnectPayment : OnboardingPaymentEvent

  data object Continue : OnboardingPaymentEvent
}
```

Presenter specifics: content maps `isConnected = session.data.hasConnectedPayinMethod`; `CollectEvents` adds:

```kotlin
        OnboardingPaymentEvent.Refresh -> launch {
          sessionStore.refreshData().onRight { refreshed ->
            currentState = OnboardingPaymentUiState.Content(
              progress = refreshed.progressFor(OnboardingStepId.ConnectPayment),
              isConnected = refreshed.data.hasConnectedPayinMethod,
            )
          }
        }
        OnboardingPaymentEvent.ConnectPayment -> navigator.openConnectPayment()
        OnboardingPaymentEvent.Continue -> launch { navigator.continueFrom(OnboardingStepId.ConnectPayment) }
```

Destination `Content` branch (include the refresh-on-resume block):

```kotlin
      is OnboardingPaymentUiState.Content -> {
        Spacer(Modifier.height(8.dp))
        if (!content.isConnected) {
          OnboardingStepHeader(
            // TODO: Add "Connect payment" / "Koppla betalning" to Lokalise
            title = "Connect payment",
            // TODO: Add "Add a payment method to activate your insurance" /
            //  "Lägg till en betalmetod för att aktivera din försäkring" to Lokalise
            description = "Add a payment method to activate your insurance",
          )
          Spacer(Modifier.weight(1f))
          // TODO: Add "Adding a payment method is required to keep your insurance active" /
          //  "Du behöver lägga till en betalmetod för att hålla din försäkring aktiv" to Lokalise
          HedvigText(
            text = "Adding a payment method is required to keep your insurance active",
            color = HedvigTheme.colorScheme.textSecondary,
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .padding(horizontal = 32.dp),
          )
          OnboardingStepButtons(
            // TODO: Add "Connect payment" / "Koppla betalning" to Lokalise
            primaryText = "Connect payment",
            onPrimaryClick = { viewModel.emit(OnboardingPaymentEvent.ConnectPayment) },
          )
        } else {
          OnboardingStepHeader(
            // TODO: Add "Connect payment" / "Koppla betalning" to Lokalise
            title = "Connect payment",
            // TODO: Add "Your payment method is connected" / "Din betalmetod är kopplad" to Lokalise
            description = "Your payment method is connected",
          )
          Spacer(Modifier.weight(1f))
          Icon(
            imageVector = HedvigIcons.Checkmark,
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterHorizontally),
          )
          Spacer(Modifier.height(16.dp))
          // TODO: Add "You can switch accounts later in settings" /
          //  "Du kan byta konto senare i inställningar" to Lokalise
          HedvigText(
            text = "You can switch accounts later in settings",
            color = HedvigTheme.colorScheme.textSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
          )
          OnboardingStepButtons(
            // TODO: Add "Continue" / "Fortsätt" to Lokalise
            primaryText = "Continue",
            onPrimaryClick = { viewModel.emit(OnboardingPaymentEvent.Continue) },
          )
        }
      }
```

- [ ] **Step 5: Run tests, format, commit**

```bash
./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding connect payment step"
```

---

### Task 18: Bundle discount step

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/bundle/OnboardingBundleDestination.kt`
- Test: `app/feature/feature-onboarding/src/test/kotlin/com/hedvig/android/feature/onboarding/ui/bundle/OnboardingBundlePresenterTest.kt`

**Interfaces:**
- Consumes: shared conventions, `OnboardingCrossSell` list from the session.
- Produces: `OnboardingBundleViewModel(sessionStore, navigator)`, `OnboardingBundleDestination(viewModel, navigateUp, openUrl: (String) -> Unit)`. `openUrl` is threaded from `:app` in Task 19 (external browser via the app's uri handler).

Figma: title "Get bundle discount", body "You get a 15% bundle discount when you have two or more insurances with us", one row per cross sell (title + description, trailing "See price" text button opening `storeUrl`), primary "Continue to app" which completes the whole flow.

- [ ] **Step 1: Write the failing presenter test**

Cases:

1. `content lists the cross sells`: default test data has one cross sell; assert `Content.crossSells.single().title == "Pet"`.
2. `continue to app completes onboarding`: BundleDiscount is the last step for default data, so `sendEvent(ContinueToApp)` must invoke the complete-use-case and clear all onboarding keys. Use a recording `FakeCompleteOnboardingUseCase` (as in Task 8's test) and a backstack seeded with `[NonOnboardingKey, OnboardingKey, OnboardingStepKey(BundleDiscount)]`; assert `completeOnboarding.invoked` is true and entries == `[NonOnboardingKey]`.

- [ ] **Step 2: Run to verify failure** (same command)

- [ ] **Step 3: Implement**

State/events:

```kotlin
internal sealed interface OnboardingBundleUiState {
  data object Loading : OnboardingBundleUiState

  data object Error : OnboardingBundleUiState

  data class Content(
    val progress: OnboardingProgress,
    val crossSells: List<OnboardingCrossSell>,
  ) : OnboardingBundleUiState
}

internal sealed interface OnboardingBundleEvent {
  data object Retry : OnboardingBundleEvent

  data object Close : OnboardingBundleEvent

  data object ContinueToApp : OnboardingBundleEvent
}
```

Presenter: content maps `crossSells = session.data.crossSells`; `ContinueToApp -> launch { navigator.continueFrom(OnboardingStepId.BundleDiscount) }` (as the last path step, `continueFrom` exits and marks seen; if a future path ever orders another step after it, this still just advances, which is correct).

Destination `Content` branch: header ("Get bundle discount" / "You get a bundle discount when you have two or more insurances with us", Lokalise TODOs; the Figma "15%" figure is not available from `otherCrossSells`, so the copy stays unquantified until backend exposes it, noted in follow-ups), then rows:

```kotlin
        for (crossSell in content.crossSells) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
          ) {
            Column(Modifier.weight(1f)) {
              HedvigText(crossSell.title)
              HedvigText(crossSell.description, color = HedvigTheme.colorScheme.textSecondary)
            }
            HedvigTextButton(
              // TODO: Add "See price" / "Se pris" to Lokalise
              text = "See price",
              onClick = { openUrl(crossSell.storeUrl) },
            )
          }
        }
```

Then `OnboardingStepButtons(primaryText = "Continue to app" /* TODO Lokalise: "Fortsätt till appen" */, onPrimaryClick = { viewModel.emit(OnboardingBundleEvent.ContinueToApp) })`.

- [ ] **Step 4: Run tests, format, commit**

```bash
./gradlew :feature-onboarding:testDebugUnitTest 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED)"
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding
git commit -m "Add onboarding bundle discount step"
```

---

### Task 19: Entries registration, gate, and :app wiring

**Files:**
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/navigation/OnboardingEntries.kt`
- Create: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/gate/OnboardingGate.kt`
- Modify: `app/app/build.gradle.kts`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigEntryProvider.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/di/ActivityRetainedGraph.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigApp.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/MainActivity.kt` (or wherever `HedvigApp(...)` is invoked; follow how `backstackController` reaches it)

**Interfaces:**
- Consumes: everything produced by Tasks 4-18; `MemberIdService`, `AuthTokenService.authStatus`/`AuthStatus.LoggedIn`, `BackstackController.currentDestination`/`pendingDeepLink` (both accessible inside `:app`), `HomeKey` (import from the home navigation module; grep `HomeKey` in `HedvigEntryProvider.kt` for the exact import), `ChipIdKey` (`com.hedvig.android.feature.chip.id.navigation`).
- Produces: `fun EntryProviderScope<HedvigNavKey>.onboardingEntries(backstack, openUrl, openPrivacyPolicy, navigateToChipId)`; `interface OnboardingGate { suspend fun shouldShowOnboarding(): Boolean }` bound in `ActivityRetainedScope`.

- [ ] **Step 1: Create the gate**

`OnboardingGate.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.gate

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.feature.onboarding.data.OnboardingSeenStore
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first

/**
 * Decides whether onboarding should be shown for the current member: never seen before, and the
 * eager fetch succeeded (caching the session so the flow renders without further loading). On
 * fetch failure this returns false WITHOUT marking seen, so the next app start retries.
 */
interface OnboardingGate {
  suspend fun shouldShowOnboarding(): Boolean
}

@ContributesBinding(ActivityRetainedScope::class)
@SingleIn(ActivityRetainedScope::class)
@Inject
internal class OnboardingGateImpl(
  private val memberIdService: MemberIdService,
  private val onboardingSeenStore: OnboardingSeenStore,
  private val sessionStore: OnboardingSessionStore,
) : OnboardingGate {
  override suspend fun shouldShowOnboarding(): Boolean {
    val memberId = memberIdService.getMemberId().first() ?: return false
    if (onboardingSeenStore.hasSeenOnboarding(memberId)) return false
    return sessionStore.getOrFetchSession().fold(
      ifLeft = { errorMessage ->
        logcat(LogPriority.INFO) { "Onboarding data fetch failed, not showing onboarding: $errorMessage" }
        false
      },
      ifRight = { session -> session.path.isNotEmpty() },
    )
  }
}
```

No dedicated unit test: every branch's collaborator (`OnboardingSeenStore`, `OnboardingSessionStore`, path builder) is already unit tested, and the composition is four lines around a concrete `MemberIdService` that has no in-repo fake. Covered by the manual QA pass in Task 20.

- [ ] **Step 2: Create the entries function**

`OnboardingEntries.kt`:

```kotlin
package com.hedvig.android.feature.onboarding.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.onboarding.ui.bundle.OnboardingBundleDestination
import com.hedvig.android.feature.onboarding.ui.bundle.OnboardingBundleViewModel
import com.hedvig.android.feature.onboarding.ui.coinsured.OnboardingCoInsuredDestination
import com.hedvig.android.feature.onboarding.ui.coinsured.OnboardingCoInsuredViewModel
import com.hedvig.android.feature.onboarding.ui.consent.OnboardingConsentDestination
import com.hedvig.android.feature.onboarding.ui.consent.OnboardingConsentViewModel
import com.hedvig.android.feature.onboarding.ui.invite.OnboardingInviteDestination
import com.hedvig.android.feature.onboarding.ui.invite.OnboardingInviteViewModel
import com.hedvig.android.feature.onboarding.ui.payment.OnboardingPaymentDestination
import com.hedvig.android.feature.onboarding.ui.payment.OnboardingPaymentViewModel
import com.hedvig.android.feature.onboarding.ui.petid.OnboardingPetIdDestination
import com.hedvig.android.feature.onboarding.ui.petid.OnboardingPetIdViewModel
import com.hedvig.android.feature.onboarding.ui.phone.OnboardingPhoneDestination
import com.hedvig.android.feature.onboarding.ui.phone.OnboardingPhoneViewModel
import com.hedvig.android.feature.onboarding.ui.theme.OnboardingThemeDestination
import com.hedvig.android.feature.onboarding.ui.theme.OnboardingThemeViewModel
import com.hedvig.android.feature.onboarding.ui.welcome.OnboardingWelcomeDestination
import com.hedvig.android.feature.onboarding.ui.welcome.OnboardingWelcomeViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.onboardingEntries(
  backstack: Backstack,
  openUrl: (String) -> Unit,
  openPrivacyPolicy: () -> Unit,
  navigateToChipId: (contractId: String) -> Unit,
) {
  entry<OnboardingKey> {
    val viewModel: OnboardingWelcomeViewModel = metroViewModel()
    OnboardingWelcomeDestination(viewModel)
  }

  entry<OnboardingStepKey> { key ->
    when (key.stepId) {
      OnboardingStepId.AnalyticsConsent -> {
        val viewModel: OnboardingConsentViewModel = metroViewModel()
        OnboardingConsentDestination(viewModel, backstack::navigateUp, openPrivacyPolicy)
      }
      OnboardingStepId.PhoneNumber -> {
        val viewModel: OnboardingPhoneViewModel = metroViewModel()
        OnboardingPhoneDestination(viewModel, backstack::navigateUp)
      }
      OnboardingStepId.Theme -> {
        val viewModel: OnboardingThemeViewModel = metroViewModel()
        OnboardingThemeDestination(viewModel, backstack::navigateUp)
      }
      OnboardingStepId.CoInsured -> {
        val viewModel: OnboardingCoInsuredViewModel = metroViewModel()
        OnboardingCoInsuredDestination(viewModel, backstack::navigateUp)
      }
      OnboardingStepId.PetIds -> {
        val viewModel: OnboardingPetIdViewModel = metroViewModel()
        OnboardingPetIdDestination(viewModel, backstack::navigateUp, navigateToChipId)
      }
      OnboardingStepId.InviteFriend -> {
        val viewModel: OnboardingInviteViewModel = metroViewModel()
        OnboardingInviteDestination(viewModel, backstack::navigateUp)
      }
      OnboardingStepId.ConnectPayment -> {
        val viewModel: OnboardingPaymentViewModel = metroViewModel()
        OnboardingPaymentDestination(viewModel, backstack::navigateUp)
      }
      OnboardingStepId.BundleDiscount -> {
        val viewModel: OnboardingBundleViewModel = metroViewModel()
        OnboardingBundleDestination(viewModel, backstack::navigateUp, openUrl)
      }
    }
  }
}
```

(No `NavSuiteSceneDecoratorStrategy.showNavBar()` metadata: onboarding entries are full-screen without the bottom nav, which is the default for entries without that metadata.)

- [ ] **Step 3: Wire into :app**

1. `app/app/build.gradle.kts`: add `implementation(projects.featureOnboarding)` to the dependencies block (alphabetical position).
2. `HedvigEntryProvider.kt`: import `com.hedvig.android.feature.onboarding.navigation.onboardingEntries` and call it where the other shared-flow entries are registered (`addSharedFlowEntries` is a fine home):

```kotlin
  onboardingEntries(
    backstack = backstack,
    openUrl = openUrl,
    openPrivacyPolicy = { openUrl(hedvigBuildConstants.urlPrivacyPolicy) },
    navigateToChipId = { contractId -> backstack.add(ChipIdKey(contractId)) },
  )
```

`hedvigBuildConstants.urlPrivacyPolicy`: check `HedvigBuildConstants` for an existing privacy-policy URL property; if none exists, grep `feature-profile`/`feature-login` for the privacy policy URL they open and reuse that exact constant or string. Thread whatever parameters (`openUrl`, `hedvigBuildConstants`) the chosen registration site already has; `hedvigEntryProvider`'s signature already includes both.

3. `ActivityRetainedGraph.kt`: add an accessor so `:app` can hand the gate to the effect:

```kotlin
  val onboardingGate: OnboardingGate
```

(import `com.hedvig.android.feature.onboarding.gate.OnboardingGate`).

4. `HedvigApp.kt`: add `onboardingGate: OnboardingGate` to the `HedvigApp(...)` parameter list; pass it at the call site the same way `backstackController` gets there (both live on the `ActivityRetainedGraph`, read in `MainActivity` via `navRetainedViewModel`). Next to `TryShowAppStoreReviewDialogEffect(...)`, add:

```kotlin
  TryShowOnboardingEffect(
    authTokenService = authTokenService,
    onboardingGate = onboardingGate,
    backstackController = backstackController,
  )
```

And the effect definition next to `TryShowAppStoreReviewDialogEffect`:

```kotlin
@Composable
private fun TryShowOnboardingEffect(
  authTokenService: AuthTokenService,
  onboardingGate: OnboardingGate,
  backstackController: BackstackController,
) {
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  LaunchedEffect(lifecycle) {
    lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
      authTokenService.authStatus.first { it is AuthStatus.LoggedIn }
      // Already showing (e.g. resumed mid-flow): nothing to do this pass.
      if (backstackController.entries.any { it is OnboardingKey || it is OnboardingStepKey }) {
        return@repeatOnLifecycle
      }
      if (!onboardingGate.shouldShowOnboarding()) return@repeatOnLifecycle
      // Only interrupt an idle Home root, never a deep-linked or in-progress flow.
      snapshotFlow { backstackController.currentDestination to backstackController.pendingDeepLink }
        .first { (currentDestination, pendingDeepLink) ->
          currentDestination is HomeKey && pendingDeepLink == null
        }
      backstackController.add(OnboardingKey)
    }
  }
}
```

`OnboardingStepKey` is `internal` to the feature module, so the `it is OnboardingStepKey` check cannot compile in `:app`; drop it and keep only `it is OnboardingKey` (the root key is always present while the flow is up, since steps are pushed on top of it). Adjust the comment accordingly. Imports mirror `TryShowAppStoreReviewDialogEffect`'s (`AuthStatus`, `first`, `repeatOnLifecycle`, `LocalLifecycleOwner`, plus `androidx.compose.runtime.snapshotFlow`, `com.hedvig.android.feature.onboarding.navigation.OnboardingKey`, and the `HomeKey` import already used elsewhere in `:app`).

- [ ] **Step 4: Verify the graph, serialization test, and full app build**

```bash
./gradlew :app:testDevelopDebugUnitTest --tests "*ExhaustiveBackstackSerializationTest*" 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED|error:)" | head -5
./gradlew :app:assembleDevelopDebug 2>&1 | grep -E "(BUILD SUCCESSFUL|FAILED|error:)" | head -5
```

Expected: both `BUILD SUCCESSFUL`. (If the test task name differs, run `./gradlew :app:tasks --all | grep -i test | head` and pick the develop-debug unit test task.) The serialization test proves both new keys round-trip via the `navKeys()`-generated `SerializersModule`. A Metro error about `OnboardingGate` here means the `@ContributesBinding(ActivityRetainedScope::class)` contribution isn't visible; re-check the interface is `public` (see the project memory: internal cross-module entry points compile but fail).

- [ ] **Step 5: Format and commit**

```bash
./gradlew ktlintFormat 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
git add app/feature/feature-onboarding app/app
git commit -m "Register onboarding entries and gate the flow after login"
```

---

### Task 20: Full verification sweep and manual QA

**Files:** none created; fixes only if verification fails.

- [ ] **Step 1: Full unit test run**

```bash
./gradlew test 2>&1 | tail -30
```

Grep the full output for `BUILD SUCCESSFUL` explicitly (do not trust the piped exit code). Fix any failures before proceeding.

- [ ] **Step 2: Lint and format check**

```bash
./gradlew ktlintCheck 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
./gradlew :feature-onboarding:lint 2>&1 | grep -E "BUILD (SUCCESSFUL|FAILED)"
```

- [ ] **Step 3: Manual QA on a develop build (staging backend)**

Install `:app:assembleDevelopDebug` on a device/emulator and verify:

1. Fresh login with a member who has never seen onboarding: after landing on Home, the flow appears on top with the progress bar sized to the member's path.
2. Back arrow pops one step; system back gesture matches it (predictive back included).
3. X on any step returns to Home; killing and relaunching the app does NOT show onboarding again (per-member seen flag).
4. Airplane mode before first login: no onboarding, no crash; disable airplane mode, restart the app: onboarding appears (fetch failure did not mark seen).
5. Co-insured "Add" opens the edit-co-insured flow; finishing it returns to the step with the row checked.
6. "Connect payment" opens Trustly; returning shows the connected variant after refresh.
7. Process death mid-flow (`adb shell am kill com.hedvig.dev.app` while backgrounded on a step): reopening restores the step, shows loading, refetches, and renders.
8. Deny consent, then trigger app actions: verify no Firebase Analytics events leave (Firebase DebugView), while Datadog RUM still reports. Then grant consent in Settings and verify buffered/live events arrive.
9. Deep link into the app (e.g. a contract deep link) with an unseen member: the deep-linked screen is NOT interrupted by onboarding; onboarding appears on a later idle-Home resume.

- [ ] **Step 4: Update follow-ups and finish**

Record these known follow-ups in the PR description (all deliberately out of v1 scope):

- Figma animations (welcome badge, consent checkmark, phone number pad, payment dots/checkmark).
- Replace every hardcoded string once the Lokalise keys exist (`grep -rn "TODO: Add" app/feature/feature-onboarding app/feature/feature-profile app/tracking` to enumerate).
- Bundle-discount percentage copy once backend exposes the figure.
- Confirm the `crossSellV2` `userFlow` input value and share-message copy with product.
- Consider persisting the consent event buffer across process death (in-memory in v1).
- Spec deviation (deliberate simplification): when a process-death refetch rebuilds a path that no
  longer contains the restored step, the spec suggested popping to the nearest preceding step; v1
  instead keeps the step visible and degrades its progress indicator (see `progressFor`). Revisit
  if it ever surfaces in practice.

Then run the finishing flow (`superpowers:finishing-a-development-branch`): final review, PR against `develop`.
