package com.hedvig.android.experiments.unleash

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.tracking.test.FakeEventTrackingClient
import com.hedvig.android.experiments.Experiment
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class UnleashExperimentProviderTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private class FakeExperimentFlags(
    val variants: MutableMap<String, String?> = mutableMapOf(),
  ) : ExperimentFlags {
    /** Completes once flag state exists. Left incomplete, the client has never fetched. */
    val ready = CompletableDeferred<Unit>()
    override val updates = MutableSharedFlow<Unit>(replay = 1)

    override suspend fun awaitReady() = ready.await()

    override fun variantOf(flagName: String): String? = variants[flagName]
  }

  private fun TestScope.provider(
    flags: FakeExperimentFlags,
    client: FakeEventTrackingClient = FakeEventTrackingClient(),
  ): UnleashExperimentProvider {
    return UnleashExperimentProvider(
      experimentFlags = flags,
      eventTrackingClient = client,
      applicationScope = ApplicationScope(backgroundScope),
      coroutineContext = StandardTestDispatcher(testScheduler),
    )
  }

  private fun flagsWithVariant(variant: String?) =
    FakeExperimentFlags(mutableMapOf("accident_discount_experiment" to variant)).apply { ready.complete(Unit) }

  @Test
  fun `the variant Unleash assigns is reported under the experiment name`() = runTest {
    assertThat(provider(flagsWithVariant("NO_DISCOUNT")).activeExperiments())
      .containsExactly(Experiment("ACCIDENT_DISCOUNT", "NO_DISCOUNT"))
    assertThat(provider(flagsWithVariant("DISCOUNT")).activeExperiments())
      .containsExactly(Experiment("ACCIDENT_DISCOUNT", "DISCOUNT"))
  }

  @Test
  fun `a flag that is off for the member reports no experiment`() = runTest {
    assertThat(provider(flagsWithVariant(null)).activeExperiments()).isEmpty()
  }

  @Test
  fun `a variant that is not SCREAMING_SNAKE_CASE is ignored rather than sent`() = runTest {
    assertThat(provider(flagsWithVariant("no discount")).activeExperiments()).isEmpty()
  }

  @Test
  fun `an install that has never fetched stops waiting after the timeout and reports no experiment`() = runTest {
    val flags = FakeExperimentFlags(mutableMapOf("accident_discount_experiment" to null))

    assertThat(provider(flags).activeExperiments()).isEmpty()
  }

  @Test
  fun `user properties are left alone until Unleash has state`() = runTest {
    val flags = FakeExperimentFlags(mutableMapOf("accident_discount_experiment" to null))
    val client = FakeEventTrackingClient()
    provider(flags, client).initialize()

    flags.updates.emit(Unit)
    runCurrent()
    assertThat(client.userProperties).isEmpty()

    flags.variants["accident_discount_experiment"] = "DISCOUNT"
    flags.ready.complete(Unit)
    runCurrent()
    assertThat(client.userProperties).containsOnly(
      "experiment_name" to "ACCIDENT_DISCOUNT",
      "experiment_variant" to "DISCOUNT",
    )
  }

  @Test
  fun `user properties follow every flag update and clear when the member leaves the experiment`() = runTest {
    val flags = flagsWithVariant("NO_DISCOUNT")
    val client = FakeEventTrackingClient()
    provider(flags, client).initialize()

    flags.updates.emit(Unit)
    runCurrent()
    assertThat(client.userProperties).containsOnly(
      "experiment_name" to "ACCIDENT_DISCOUNT",
      "experiment_variant" to "NO_DISCOUNT",
    )

    flags.variants["accident_discount_experiment"] = null
    flags.updates.emit(Unit)
    runCurrent()
    assertThat(client.userProperties).containsOnly(
      "experiment_name" to null,
      "experiment_variant" to null,
    )
  }
}
