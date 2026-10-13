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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class FirebaseExperimentProviderTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  /** Bucket 67 for the accident experiment, so in the treatment only at 68% and above. */
  private val memberInBucket67 = "123456"

  /** Bucket 47 for the accident experiment, so in the treatment at 48% and above. */
  private val memberInBucket47 = "member-abc"

  private class FakeRemoteConfig(
    val activeValues: MutableMap<String, Long> = mutableMapOf(),
    override var hasFetchedBefore: Boolean = false,
  ) : ExperimentRemoteConfig {
    /** What the fetch does once it is awaited. Left incomplete, the fetch never finishes. */
    val fetch = CompletableDeferred<Unit>()
    var fetchCalled = false

    override suspend fun fetchAndActivate() {
      fetchCalled = true
      fetch.await()
    }

    override fun getLong(key: String): Long = activeValues[key] ?: 0L
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

  private fun TestScope.provider(
    remoteConfig: FakeRemoteConfig,
    client: RecordingClient = RecordingClient(),
    memberIds: MutableStateFlow<String?> = MutableStateFlow(memberInBucket47),
  ): FirebaseExperimentProvider {
    return FirebaseExperimentProvider(
      remoteConfig = remoteConfig,
      eventTrackingClient = client,
      applicationScope = ApplicationScope(backgroundScope),
      coroutineContext = StandardTestDispatcher(testScheduler),
      memberIds = memberIds,
    )
  }

  private fun accidentPercent(percent: Long) = FakeRemoteConfig(mutableMapOf("accident_no_discount_percent" to percent))

  @Test
  fun `the bucket is the documented FNV-1a hash of the experiment name and member id`() {
    assertThat(fnv1a32("ACCIDENT_DISCOUNT:123456".encodeToByteArray())).isEqualTo(1311234767u)
    assertThat(experimentBucket("ACCIDENT_DISCOUNT", memberInBucket67)).isEqualTo(67)
    assertThat(fnv1a32("ACCIDENT_DISCOUNT:member-abc".encodeToByteArray())).isEqualTo(1936731147u)
    assertThat(experimentBucket("ACCIDENT_DISCOUNT", memberInBucket47)).isEqualTo(47)
  }

  @Test
  fun `a member whose bucket is below the percentage is in the treatment`() = runTest {
    val remoteConfig = accidentPercent(50)
    val provider = provider(remoteConfig, memberIds = MutableStateFlow(memberInBucket47))
    provider.initialize()
    remoteConfig.fetch.complete(Unit)

    assertThat(provider.activeExperiments()).containsExactly(Experiment("ACCIDENT_DISCOUNT", "NO_DISCOUNT"))
  }

  @Test
  fun `a member whose bucket is at or above the percentage is in the control`() = runTest {
    val remoteConfig = accidentPercent(50)
    val provider = provider(remoteConfig, memberIds = MutableStateFlow(memberInBucket67))
    provider.initialize()
    remoteConfig.fetch.complete(Unit)

    assertThat(provider.activeExperiments()).containsExactly(Experiment("ACCIDENT_DISCOUNT", "CONTROL"))
  }

  @Test
  fun `the percentage boundary is inclusive for the control`() = runTest {
    assertThat(provider(accidentPercent(47), memberIds = MutableStateFlow(memberInBucket47)).activeExperiments())
      .containsExactly(Experiment("ACCIDENT_DISCOUNT", "CONTROL"))
    assertThat(provider(accidentPercent(48), memberIds = MutableStateFlow(memberInBucket47)).activeExperiments())
      .containsExactly(Experiment("ACCIDENT_DISCOUNT", "NO_DISCOUNT"))
  }

  @Test
  fun `zero percent means the experiment is off and nothing is sent`() = runTest {
    val remoteConfig = accidentPercent(0)
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
  fun `a percentage outside 0 to 100 is clamped`() = runTest {
    assertThat(provider(accidentPercent(250)).activeExperiments())
      .containsExactly(Experiment("ACCIDENT_DISCOUNT", "NO_DISCOUNT"))
    assertThat(provider(accidentPercent(-5)).activeExperiments()).isEmpty()
  }

  @Test
  fun `the active variant is set as user properties and follows the member id`() = runTest {
    val remoteConfig = accidentPercent(50)
    val client = RecordingClient()
    val memberIds = MutableStateFlow<String?>(memberInBucket47)
    val provider = provider(remoteConfig, client, memberIds)
    provider.initialize()
    remoteConfig.fetch.complete(Unit)
    runCurrent()
    assertThat(client.userProperties).containsOnly(
      "experiment_name" to "ACCIDENT_DISCOUNT",
      "experiment_variant" to "NO_DISCOUNT",
    )

    memberIds.value = memberInBucket67
    runCurrent()
    assertThat(client.userProperties).containsOnly(
      "experiment_name" to "ACCIDENT_DISCOUNT",
      "experiment_variant" to "CONTROL",
    )

    memberIds.value = null
    runCurrent()
    assertThat(client.userProperties).containsOnly(
      "experiment_name" to null,
      "experiment_variant" to null,
    )
  }

  @Test
  fun `logged out, nothing is sent even when the experiment is on`() = runTest {
    val provider = provider(accidentPercent(100), memberIds = MutableStateFlow(null))

    assertThat(provider.activeExperiments()).isEmpty()
  }

  @Test
  fun `a failed fetch falls back to the last activated percentage`() = runTest {
    val remoteConfig = accidentPercent(50)
    val provider = provider(remoteConfig)
    provider.initialize()
    remoteConfig.fetch.completeExceptionally(IllegalStateException("offline"))

    assertThat(provider.activeExperiments()).containsExactly(Experiment("ACCIDENT_DISCOUNT", "NO_DISCOUNT"))
  }

  @Test
  fun `a fetch that never finishes stops blocking after the timeout`() = runTest {
    val remoteConfig = FakeRemoteConfig()
    val provider = provider(remoteConfig)
    provider.initialize()

    assertThat(provider.activeExperiments()).isEmpty()
    assertThat(remoteConfig.fetchCalled).isTrue()
    assertThat(remoteConfig.fetch.isCompleted).isFalse()
  }

  @Test
  fun `once the install has fetched before, a slow startup fetch does not delay the answer`() = runTest {
    val remoteConfig = accidentPercent(50).apply { hasFetchedBefore = true }
    val provider = provider(remoteConfig)
    provider.initialize()

    assertThat(provider.activeExperiments()).containsExactly(Experiment("ACCIDENT_DISCOUNT", "NO_DISCOUNT"))
    assertThat(testScheduler.currentTime).isEqualTo(0)
    assertThat(remoteConfig.fetch.isCompleted).isFalse()
  }
}
