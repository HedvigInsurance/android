package com.hedvig.android.tracking.firebase

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.key
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ConsentAwareEventTrackingClientTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private class RecordingClient : EventTrackingClient {
    val events = mutableListOf<Pair<String, Map<String, Any?>>>()
    val screens = mutableListOf<String>()
    var recordedUserId: String? = null
    val collectionEnabledCalls = mutableListOf<Boolean>()

    override fun setCollectionEnabled(enabled: Boolean) {
      collectionEnabledCalls.add(enabled)
    }

    override fun trackEvent(name: String, parameters: Map<String, Any?>) {
      events.add(name to parameters)
    }

    override fun trackScreen(name: String, screenClass: String?, parameters: Map<String, Any?>) {
      screens.add(name)
    }

    override fun setUserId(userId: String?) {
      recordedUserId = userId
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
    assertThat(recording.recordedUserId).isEqualTo("member-id")
  }

  @Test
  fun `denying consent disables SDK collection and granting re-enables it`() = runTest {
    val recording = RecordingClient()
    val settings = FakeSettingsDataStore()
    client(recording, settings)
    runCurrent()
    settings.consent.value = AnalyticsConsent.DENIED
    runCurrent()
    assertThat(recording.collectionEnabledCalls.last()).isEqualTo(false)
    settings.consent.value = AnalyticsConsent.GRANTED
    runCurrent()
    assertThat(recording.collectionEnabledCalls.last()).isEqualTo(true)
  }

  @Test
  fun `demo mode wins over granted consent`() = runTest {
    val recording = RecordingClient()
    val settings = FakeSettingsDataStore()
    val client = client(recording, settings)
    runCurrent()
    client.setCollectionEnabled(false)
    settings.consent.value = AnalyticsConsent.GRANTED
    runCurrent()
    assertThat(recording.collectionEnabledCalls.last()).isEqualTo(false)
  }

  @Test
  fun `enabling collection while denied stays disabled`() = runTest {
    val recording = RecordingClient()
    val settings = FakeSettingsDataStore()
    val client = client(recording, settings)
    runCurrent()
    settings.consent.value = AnalyticsConsent.DENIED
    runCurrent()
    client.setCollectionEnabled(true)
    assertThat(recording.collectionEnabledCalls.last()).isEqualTo(false)
  }
}
