package com.hedvig.android.desktop

import androidx.lifecycle.ViewModel
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.experiments.Experiment
import com.hedvig.android.experiments.ExperimentProvider
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass

/**
 * Resolves every ViewModel from the desktop graph's maps. The graph holds both scopes, so one factory sees them all.
 */
@ContributesBinding(AppScope::class)
@Inject
internal class DesktopViewModelFactory(
  override val viewModelProviders: Map<KClass<out ViewModel>, () -> ViewModel>,
  override val assistedFactoryProviders: Map<KClass<out ViewModel>, () -> ViewModelAssistedFactory>,
  override val manualAssistedFactoryProviders:
    Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory>,
) : MetroViewModelFactory()

/**
 * Sends nothing anywhere. Logs each call at DEBUG, so the log shows what the app would have tracked.
 */
@ContributesBinding(AppScope::class)
@Inject
internal class LoggingEventTrackingClient : EventTrackingClient {
  override fun setCollectionEnabled(enabled: Boolean) {
    logcat(LogPriority.DEBUG) { "Tracking: setCollectionEnabled($enabled) not sent" }
  }

  override fun trackEvent(name: String, parameters: Map<String, Any?>) {
    logcat(LogPriority.DEBUG) { "Tracking: event $name $parameters not sent" }
  }

  override fun trackScreen(name: String, screenClass: String?, parameters: Map<String, Any?>) {
    logcat(LogPriority.DEBUG) { "Tracking: screen $name ($screenClass) $parameters not sent" }
  }

  override fun setUserId(userId: String?) {
    logcat(LogPriority.DEBUG) { "Tracking: setUserId not sent" }
  }

  override fun setUserProperty(name: String, value: String?) {
    logcat(LogPriority.DEBUG) { "Tracking: user property $name not sent" }
  }
}

/** Puts the member in no experiment, so the backend answers with its defaults. */
@ContributesBinding(AppScope::class)
@Inject
internal class NoActiveExperimentsProvider : ExperimentProvider {
  override suspend fun activeExperiments(): List<Experiment> {
    logcat(LogPriority.DEBUG) { "Experiments are off on desktop, reporting none" }
    return emptyList()
  }
}
