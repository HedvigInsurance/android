package com.hedvig.android.experiments.unleash

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.experiments.Experiment
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
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
    /** Completes once the toggles in hand are the current member's own. Left incomplete, they aren't yet. */
    val memberState = CompletableDeferred<Unit>()
    override val updates = MutableSharedFlow<Unit>(replay = 1)

    override fun hasStateForCurrentMember(): Boolean = memberState.isCompleted

    override suspend fun awaitStateForCurrentMember() = memberState.await()

    override fun variantOf(flagName: String): String? = if (hasStateForCurrentMember()) variants[flagName] else null

    /** The member's own toggles arriving, which in the real client is a state change and so also an update. */
    suspend fun receiveMemberState() {
      memberState.complete(Unit)
      updates.emit(Unit)
    }
  }

  private fun TestScope.provider(flags: FakeExperimentFlags): UnleashExperimentProvider {
    return UnleashExperimentProvider(
      experimentFlags = flags,
      applicationScope = ApplicationScope(backgroundScope),
      coroutineContext = StandardTestDispatcher(testScheduler),
    )
  }

  private fun flagsWithVariant(variant: String?) =
    FakeExperimentFlags(mutableMapOf("accident_discount_experiment" to variant)).apply { memberState.complete(Unit) }

  @Test
  fun `the variant Unleash assigns is reported under the experiment name`() = runTest {
    assertThat(provider(flagsWithVariant("NO_DISCOUNT")).activeExperiments())
      .containsExactly(Experiment("ACCIDENT_DISCOUNT", "NO_DISCOUNT"))
    assertThat(provider(flagsWithVariant("DISCOUNT_50")).activeExperiments())
      .containsExactly(Experiment("ACCIDENT_DISCOUNT", "DISCOUNT_50"))
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
  fun `without the member's own toggles it stops waiting after the timeout and reports no experiment`() = runTest {
    val flags = FakeExperimentFlags(mutableMapOf("accident_discount_experiment" to null))

    assertThat(provider(flags).activeExperiments()).isEmpty()
  }

  @Test
  fun `after one timed-out wait, later requests answer at once until the flags change`() = runTest {
    val flags = FakeExperimentFlags(mutableMapOf("accident_discount_experiment" to null))
    val provider = provider(flags).apply { initialize() }
    runCurrent()

    val firstStart = currentTime
    provider.activeExperiments()
    assertThat(currentTime - firstStart).isEqualTo(3_000L)

    val secondStart = currentTime
    provider.activeExperiments()
    assertThat(currentTime - secondStart).isEqualTo(0L)

    flags.updates.emit(Unit)
    runCurrent()
    val thirdStart = currentTime
    provider.activeExperiments()
    assertThat(currentTime - thirdStart).isEqualTo(3_000L)
  }
}
