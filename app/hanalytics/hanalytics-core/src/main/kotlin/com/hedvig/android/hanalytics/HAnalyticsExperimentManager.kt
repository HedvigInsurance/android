package com.hedvig.android.hanalytics

import com.hedvig.hanalytics.HAnalyticsExperiment
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface HAnalyticsExperimentManager {
  suspend fun getExperiment(name: String): HAnalyticsExperiment
  suspend fun invalidateExperiments()
}

internal class HAnalyticsExperimentManagerImpl(
  private val hAnalyticsExperimentStorage: HAnalyticsExperimentStorage,
  private val hAnalyticsService: HAnalyticsService,
) : HAnalyticsExperimentManager {
  private val mutex = Mutex()

  /**
   * Throwing an exception is a normal behavior, which will result in the experiment returning the default value.
   */
  override suspend fun getExperiment(name: String): HAnalyticsExperiment {
    return mutex.withLock {
      try {
        hAnalyticsExperimentStorage.getExperiment(name)
      } catch (e: IllegalStateException) {
        loadExperimentsFromServer()
        hAnalyticsExperimentStorage.getExperiment(name)
      }
    }
  }

  override suspend fun invalidateExperiments() {
    hAnalyticsExperimentStorage.invalidateExperiments()
  }

  private suspend fun loadExperimentsFromServer() {
    val experimentsList = hAnalyticsService.getExperiments() ?: return
    hAnalyticsExperimentStorage.updateExperiments(experimentsList)
  }
}
