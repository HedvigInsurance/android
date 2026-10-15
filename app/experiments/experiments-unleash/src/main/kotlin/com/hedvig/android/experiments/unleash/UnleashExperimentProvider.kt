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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reports the member's experiment variants as Unleash assigns them, see [ExperimentDefinition].
 *
 * A variant is only reported from toggles fetched for the current member, since Unleash assigns it from the
 * member id the fetch carried. Those toggles are usually in hand at once, persisted from the member's last
 * session. Right after a login, or on a first launch, [activeExperiments] waits for the member's own fetch for
 * at most [MEMBER_STATE_TIMEOUT], after which it reports no experiments and the member keeps today's behaviour.
 *
 * The variants are also set as Firebase user properties so every analytics event carries them, and they follow
 * every flag update, which includes the member id in the Unleash context changing on login and logout. User
 * properties bypass the analytics consent buffer by design, and Firebase uploads nothing while collection is
 * disabled, so no extra gating is needed here. Firebase drops user property values longer than 36 characters.
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
  override fun initialize() {
    applicationScope.launch(coroutineContext) {
      // Toggles that aren't the member's own say nothing about their group. Publishing from them would clear the
      // properties Firebase kept from the last session, leaving events without the group until the fetch lands.
      experimentFlags.updates
        .filter { experimentFlags.hasStateForCurrentMember() }
        .map { currentExperiments() }
        .distinctUntilChanged()
        .collect(::publishUserProperties)
    }
  }

  override suspend fun activeExperiments(): List<Experiment> {
    withTimeoutOrNull(MEMBER_STATE_TIMEOUT) { experimentFlags.awaitStateForCurrentMember() }
    return currentExperiments()
  }

  private fun currentExperiments(): List<Experiment> {
    return knownExperiments.mapNotNull { definition ->
      val variant = experimentFlags.variantOf(definition.unleashFlag) ?: return@mapNotNull null
      if (!variant.matches(SCREAMING_SNAKE_CASE)) {
        // The backend expects this shape, and a mistyped variant in the Unleash admin must not reach it.
        logcat(LogPriority.WARN) { "Ignoring ${definition.name} variant '$variant': not SCREAMING_SNAKE_CASE" }
        return@mapNotNull null
      }
      Experiment(name = definition.name, variant = variant)
    }
  }

  // Two properties hold one experiment at a time, which is all that is running. Cleared when the member is in none.
  private fun publishUserProperties(experiments: List<Experiment>) {
    val experiment = experiments.firstOrNull()
    eventTrackingClient.setUserProperty(USER_PROPERTY_EXPERIMENT_NAME, experiment?.name)
    eventTrackingClient.setUserProperty(USER_PROPERTY_EXPERIMENT_VARIANT, experiment?.variant)
  }

  companion object {
    private val MEMBER_STATE_TIMEOUT = 3.seconds
    private val SCREAMING_SNAKE_CASE = Regex("[A-Z0-9_]+")
    internal const val USER_PROPERTY_EXPERIMENT_NAME = "experiment_name"
    internal const val USER_PROPERTY_EXPERIMENT_VARIANT = "experiment_variant"
  }
}
