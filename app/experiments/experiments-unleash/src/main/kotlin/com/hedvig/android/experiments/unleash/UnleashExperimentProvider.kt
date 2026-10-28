package com.hedvig.android.experiments.unleash

import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.IoDispatcher
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.experiments.Experiment
import com.hedvig.android.experiments.ExperimentProvider
import com.hedvig.android.initializable.Initializable
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reports the member's experiment variants as Unleash assigns them, see [ExperimentDefinition].
 *
 * A variant is only reported from toggles fetched for the current member, since Unleash assigns it from the
 * member id the fetch carried. Those toggles are usually in hand at once, persisted from the member's last
 * session. Right after a login, or on a first launch, [activeExperiments] waits for the member's own fetch for
 * at most [MEMBER_STATE_TIMEOUT], after which it reports no experiments and the member keeps today's behaviour.
 * Once a wait has timed out, later calls answer at once until the toggles or the member change, so an unreachable
 * Unleash delays one request rather than every one.
 *
 * Each experiment is also set as a Firebase user property named after it, holding the variant, so every analytics
 * event carries it. They follow every flag update, which includes the member id in the Unleash context changing on
 * login and logout. User properties bypass the analytics consent buffer by design, and Firebase uploads nothing while
 * collection is disabled, so no extra gating is needed here.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<ExperimentProvider>())
@ContributesIntoSet(AppScope::class, binding = binding<Initializable>())
@Inject
internal class UnleashExperimentProvider(
  private val experimentFlags: ExperimentFlags,
  private val eventTrackingClient: EventTrackingClient,
  private val applicationScope: ApplicationScope,
  @IoDispatcher private val coroutineContext: CoroutineContext,
) : ExperimentProvider, Initializable {
  /** Counts [ExperimentFlags.updates], so a timed-out wait is only skipped while nothing has changed since. */
  @Volatile
  private var updateGeneration = 0L

  @Volatile
  private var timedOutAtGeneration: Long? = null

  override fun initialize() {
    applicationScope.launch(coroutineContext) {
      // Toggles that aren't the member's own say nothing about their group. Publishing from them would clear the
      // properties Firebase kept from the last session, leaving events without the group until the fetch lands.
      experimentFlags.updates
        .onEach { updateGeneration++ }
        .filter { experimentFlags.hasStateForCurrentMember() }
        .map { currentExperiments() }
        .distinctUntilChanged()
        .collect(::publishUserProperties)
    }
  }

  override suspend fun activeExperiments(): List<Experiment> {
    val generation = updateGeneration
    if (timedOutAtGeneration != generation) {
      val arrived = withTimeoutOrNull(MEMBER_STATE_TIMEOUT) { experimentFlags.awaitStateForCurrentMember() } != null
      if (!arrived) {
        timedOutAtGeneration = generation
        // Logged once per timed-out wait, so Datadog can count how often a member's group goes unreported.
        logcat(LogPriority.INFO) {
          "Unleash toggles for the current member did not arrive within $MEMBER_STATE_TIMEOUT, reporting no experiments"
        }
      }
    }
    return currentExperiments()
  }

  private fun currentExperiments(): List<Experiment> {
    return knownExperiments.mapNotNull { definition ->
      val variant = experimentFlags.variantOf(definition.unleashFlag) ?: return@mapNotNull null
      if (!variant.matches(SCREAMING_SNAKE_CASE)) {
        // The backend expects this shape, and a mistyped variant in the Unleash admin must not reach it.
        logcat(LogPriority.ERROR) { "Ignoring ${definition.name} variant '$variant': not SCREAMING_SNAKE_CASE" }
        return@mapNotNull null
      }
      Experiment(name = definition.name, variant = variant)
    }
  }

  // Every known experiment is written, so one the member is not in is cleared rather than kept from an earlier session.
  private fun publishUserProperties(experiments: List<Experiment>) {
    for (definition in knownExperiments) {
      val variant = experiments.firstOrNull { it.name == definition.name }?.variant
      eventTrackingClient.setUserProperty(definition.name, variant)
    }
  }

  companion object {
    private val MEMBER_STATE_TIMEOUT = 3.seconds
    private val SCREAMING_SNAKE_CASE = Regex("[A-Z0-9_]+")
  }
}
