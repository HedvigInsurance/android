package com.hedvig.android.hanalytics

import com.hedvig.hanalytics.HAnalyticsExperiment
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface HAnalyticsExperimentManager {
  suspend fun getExperiment(name: String): HAnalyticsExperiment
  suspend fun invalidateExperiments()
}

internal class HAnalyticsExperimentManagerImpl(
  private val hAnalyticsService: HAnalyticsService,
) : HAnalyticsExperimentManager {
  private val mutex = Mutex()
  private val experimentsData = mutableMapOf<String, String>()

  /**
   * Throwing an exception is a normal behavior, which will result in the experiment returning the default value.
   */
  override suspend fun getExperiment(name: String): HAnalyticsExperiment {
    return mutex.withLock {
      if (experimentsData.isEmpty()) {
        loadExperimentsFromServer()
      }

      experimentsData[name]?.let { variant ->
        HAnalyticsExperiment(name, variant)
      } ?: error("experiment unavailable")
    }
  }

  override suspend fun invalidateExperiments() {
    mutex.withLock {
      experimentsData.clear()
    }
  }

  private suspend fun loadExperimentsFromServer() {
    val experimentsList = hAnalyticsService.getExperiments() ?: return
    experimentsData.clear()
    experimentsData.putAll(experimentsList.map { it.name to it.variant })
  }
}
