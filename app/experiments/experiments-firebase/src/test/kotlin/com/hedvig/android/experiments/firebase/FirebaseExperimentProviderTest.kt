package com.hedvig.android.experiments.firebase

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.experiments.Experiment
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class FirebaseExperimentProviderTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private class FakeRemoteConfig(
    val activeValues: MutableMap<String, String> = mutableMapOf(),
    override var hasFetchedBefore: Boolean = false,
  ) : ExperimentRemoteConfig {
    /** What the fetch does once it is awaited. Left incomplete, the fetch never finishes. */
    val fetch = CompletableDeferred<Unit>()
    var fetchCalled = false

    override suspend fun fetchAndActivate() {
      fetchCalled = true
      fetch.await()
    }

    override fun getString(key: String): String = activeValues[key] ?: ""
  }

  private class RecordingClient : EventTrackingClient {
    val userProperties = mutableMapOf<String, String?>()

    override fun setCollectionEnabled(enabled: Boolean) = error("unused")

    override fun trackEvent(name: String, parameters: Map<String, Any?>) = error("unused")

    override fun trackScreen(name: String, screenClass: String?, parameters: Map<String, Any?>) = error("unused")

    override fun setUserId(userId: String?) = error("unused")

    override fun setUserProperty(name: String, value: String?) {
      userProperties[name] = value
    }
  }

  private fun TestScope.provider(remoteConfig: FakeRemoteConfig, client: RecordingClient): FirebaseExperimentProvider {
    return FirebaseExperimentProvider(
      remoteConfig = remoteConfig,
      eventTrackingClient = client,
      applicationScope = ApplicationScope(backgroundScope),
      coroutineContext = StandardTestDispatcher(testScheduler),
    )
  }

  @Test
  fun `a fetched variant is reported under the experiment's name and set as user properties`() = runTest {
    val remoteConfig = FakeRemoteConfig(mutableMapOf("accident_discount_variant" to "DISCOUNT"))
    val client = RecordingClient()
    val provider = provider(remoteConfig, client)
    provider.initialize()
    remoteConfig.fetch.complete(Unit)

    assertThat(provider.activeExperiments()).containsExactly(Experiment("ACCIDENT_DISCOUNT", "DISCOUNT"))
    assertThat(client.userProperties).containsOnly(
      "experiment_name" to "ACCIDENT_DISCOUNT",
      "experiment_variant" to "DISCOUNT",
    )
  }

  @Test
  fun `a blank variant means no experiment and clears the user properties`() = runTest {
    val remoteConfig = FakeRemoteConfig(mutableMapOf("accident_discount_variant" to " "))
    val client = RecordingClient()
    val provider = provider(remoteConfig, client)
    provider.initialize()
    remoteConfig.fetch.complete(Unit)

    assertThat(provider.activeExperiments()).isEmpty()
    assertThat(client.userProperties).containsOnly(
      "experiment_name" to null,
      "experiment_variant" to null,
    )
  }

  @Test
  fun `a variant typed in lowercase is sent in SCREAMING_SNAKE_CASE`() = runTest {
    val remoteConfig = FakeRemoteConfig(mutableMapOf("accident_discount_variant" to " control "))
    val provider = provider(remoteConfig, RecordingClient())
    provider.initialize()
    remoteConfig.fetch.complete(Unit)

    assertThat(provider.activeExperiments()).containsExactly(Experiment("ACCIDENT_DISCOUNT", "CONTROL"))
  }

  @Test
  fun `a failed fetch falls back to the last activated variant`() = runTest {
    val remoteConfig = FakeRemoteConfig(mutableMapOf("accident_discount_variant" to "CONTROL"))
    val provider = provider(remoteConfig, RecordingClient())
    provider.initialize()
    remoteConfig.fetch.completeExceptionally(IllegalStateException("offline"))

    assertThat(provider.activeExperiments()).containsExactly(Experiment("ACCIDENT_DISCOUNT", "CONTROL"))
  }

  @Test
  fun `a fetch that never finishes stops blocking after the timeout`() = runTest {
    val remoteConfig = FakeRemoteConfig()
    val provider = provider(remoteConfig, RecordingClient())
    provider.initialize()

    assertThat(provider.activeExperiments()).isEmpty()
    assertThat(remoteConfig.fetchCalled).isTrue()
    assertThat(remoteConfig.fetch.isCompleted).isFalse()
  }

  @Test
  fun `without a startup fetch the active values are answered after the timeout`() = runTest {
    val remoteConfig = FakeRemoteConfig(mutableMapOf("accident_discount_variant" to "DISCOUNT"))
    val provider = provider(remoteConfig, RecordingClient())

    assertThat(provider.activeExperiments()).containsExactly(Experiment("ACCIDENT_DISCOUNT", "DISCOUNT"))
    assertThat(remoteConfig.fetchCalled).isFalse()
  }

  @Test
  fun `a variant that is not SCREAMING_SNAKE_CASE is ignored rather than sent`() = runTest {
    val remoteConfig = FakeRemoteConfig(mutableMapOf("accident_discount_variant" to "Discount 20%"))
    val provider = provider(remoteConfig, RecordingClient())
    provider.initialize()
    remoteConfig.fetch.complete(Unit)

    assertThat(provider.activeExperiments()).isEmpty()
  }

  @Test
  fun `once the install has fetched before, a slow startup fetch does not delay the answer`() = runTest {
    val remoteConfig = FakeRemoteConfig(
      mutableMapOf("accident_discount_variant" to "DISCOUNT"),
      hasFetchedBefore = true,
    )
    val provider = provider(remoteConfig, RecordingClient())
    provider.initialize()

    assertThat(provider.activeExperiments()).containsExactly(Experiment("ACCIDENT_DISCOUNT", "DISCOUNT"))
    assertThat(testScheduler.currentTime).isEqualTo(0)
    assertThat(remoteConfig.fetch.isCompleted).isFalse()
  }
}
