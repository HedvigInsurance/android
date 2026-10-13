package com.hedvig.android.experiments.firebase

import com.hedvig.android.auth.MemberIdService
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Derives the member's experiment variants from their member id and the percentages Remote Config serves, see
 * [ExperimentDefinition].
 *
 * Remote Config persists what was last activated, so an install keeps its percentages across restarts, offline
 * launches and failed fetches without any storage of our own. Once an install has fetched successfully,
 * [activeExperiments] answers from those activated values right away and the startup fetch refreshes them for
 * later queries. Only an install that has never fetched waits for the startup fetch, and only for
 * [STARTUP_FETCH_TIMEOUT], after which the in-code defaults mean every experiment is off.
 *
 * The active variants are also set as Firebase user properties so every analytics event carries them, which lets
 * the experiment's members be isolated in BigQuery without adding parameters to each event. They are republished
 * whenever the member id changes, since the variant is a function of it. User properties are identity calls that
 * bypass the analytics consent buffer by design, and Firebase uploads nothing while collection is disabled, so no
 * extra gating is needed here. Firebase drops user property values longer than 36 characters, so variant names must
 * stay within that.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<ExperimentProvider>())
@ContributesIntoSet(AppScope::class, binding = binding<Initializable>())
internal class FirebaseExperimentProvider(
  private val remoteConfig: ExperimentRemoteConfig,
  private val eventTrackingClient: EventTrackingClient,
  private val applicationScope: ApplicationScope,
  private val coroutineContext: CoroutineContext,
  private val memberIds: Flow<String?>,
) : ExperimentProvider, Initializable {
  @Inject
  constructor(
    remoteConfig: ExperimentRemoteConfig,
    eventTrackingClient: EventTrackingClient,
    applicationScope: ApplicationScope,
    @IoDispatcher coroutineContext: CoroutineContext,
    memberIdService: MemberIdService,
  ) : this(remoteConfig, eventTrackingClient, applicationScope, coroutineContext, memberIdService.getMemberId())

  private val startupFetch = CompletableDeferred<Unit>()

  /** Fetches and activates once, at app start, then keeps the user properties in step with the member id. */
  override fun initialize() {
    applicationScope.launch(coroutineContext) {
      try {
        remoteConfig.fetchAndActivate()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat(LogPriority.WARN, e) { "Remote Config fetch failed, keeping the last activated experiments" }
      } finally {
        startupFetch.complete(Unit)
      }
      memberIds.collect { memberId ->
        publishUserProperties(activatedExperiments(memberId))
      }
    }
  }

  override suspend fun activeExperiments(): List<Experiment> {
    if (!remoteConfig.hasFetchedBefore) {
      withTimeoutOrNull(STARTUP_FETCH_TIMEOUT) { startupFetch.await() }
    }
    return activatedExperiments(memberIds.first())
  }

  /** Empty while logged out: without a member id there is nothing to bucket on, and nothing to attribute. */
  private fun activatedExperiments(memberId: String?): List<Experiment> {
    if (memberId == null) return emptyList()
    return knownExperiments.mapNotNull { definition ->
      val percent = remoteConfig.getLong(definition.remoteConfigKey).coerceIn(0L, 100L)
      if (percent == 0L) return@mapNotNull null
      val inTreatment = experimentBucket(definition.name, memberId) < percent
      Experiment(name = definition.name, variant = if (inTreatment) definition.treatment else definition.control)
    }
  }

  // Two properties hold one experiment at a time, which is all that is running. Cleared when the member is in none.
  private fun publishUserProperties(experiments: List<Experiment>) {
    val experiment = experiments.firstOrNull()
    eventTrackingClient.setUserProperty(USER_PROPERTY_EXPERIMENT_NAME, experiment?.name)
    eventTrackingClient.setUserProperty(USER_PROPERTY_EXPERIMENT_VARIANT, experiment?.variant)
  }

  companion object {
    private val STARTUP_FETCH_TIMEOUT = 3.seconds
    internal const val USER_PROPERTY_EXPERIMENT_NAME = "experiment_name"
    internal const val USER_PROPERTY_EXPERIMENT_VARIANT = "experiment_variant"
  }
}
