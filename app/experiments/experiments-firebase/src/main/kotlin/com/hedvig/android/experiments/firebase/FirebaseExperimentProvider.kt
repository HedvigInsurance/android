package com.hedvig.android.experiments.firebase

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reads the member's experiment variants out of Firebase Remote Config, which is how Firebase A/B Testing reaches
 * the app.
 *
 * Remote Config persists what was last activated, so a member keeps their variant across restarts, offline
 * launches and failed fetches without any storage of our own. [activeExperiments] therefore only waits for the
 * startup fetch, and only for [STARTUP_FETCH_TIMEOUT]: a slow or failed fetch falls back to the previously
 * activated values, and a fresh install falls back to the in-code defaults, meaning no experiment.
 *
 * The active variants are also set as Firebase user properties so every analytics event carries them, which lets
 * the experiment's members be isolated in BigQuery without adding parameters to each event. User properties are
 * identity calls that bypass the analytics consent buffer by design, and Firebase uploads nothing while collection
 * is disabled, so no extra gating is needed here.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<ExperimentProvider>())
@ContributesIntoSet(AppScope::class, binding = binding<Initializable>())
@Inject
internal class FirebaseExperimentProvider(
  private val remoteConfig: ExperimentRemoteConfig,
  private val eventTrackingClient: EventTrackingClient,
  private val applicationScope: ApplicationScope,
  @IoDispatcher private val coroutineContext: CoroutineContext,
) : ExperimentProvider, Initializable {
  private val startupFetch = CompletableDeferred<Unit>()

  /** Fetches and activates once, at app start. */
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
      publishUserProperties(activatedExperiments())
    }
  }

  override suspend fun activeExperiments(): List<Experiment> {
    withTimeoutOrNull(STARTUP_FETCH_TIMEOUT) { startupFetch.await() }
    return activatedExperiments()
  }

  private fun activatedExperiments(): List<Experiment> {
    return knownExperiments.mapNotNull { definition ->
      // Uppercased so a lowercase variant typed into the console still reaches the backend in the case it requires.
      val variant = remoteConfig.getString(definition.remoteConfigKey).trim().uppercase()
      if (variant.isEmpty()) null else Experiment(name = definition.name, variant = variant)
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
