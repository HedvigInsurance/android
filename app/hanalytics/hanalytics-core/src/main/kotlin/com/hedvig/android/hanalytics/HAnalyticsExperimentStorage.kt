package com.hedvig.android.hanalytics

import com.hedvig.android.logger.logcat
import com.hedvig.hanalytics.HAnalyticsExperiment
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * This does *not* take care of refreshing the experiments when one is not found. Use [HAnalyticsExperimentManager] for
 * that instead.
 */
internal interface HAnalyticsExperimentStorage {
  suspend fun getExperiment(name: String): HAnalyticsExperiment
  suspend fun updateExperiments(experiments: List<Experiment>)
  suspend fun invalidateExperiments()
}

internal class HAnalyticsExperimentStorageImpl : HAnalyticsExperimentStorage {
  private val mutex = Mutex()
  private val experimentsData = mutableMapOf<String, String>()

  /**
   * Throwing an exception is a normal behavior, which will result in the experiment returning the default value.
   */
  override suspend fun getExperiment(name: String): HAnalyticsExperiment {
    return mutex.withLock {
      experimentsData[name]?.let { variant ->
        HAnalyticsExperiment(name, variant)
      } ?: error("experiment unavailable")
    }
  }

  override suspend fun updateExperiments(experiments: List<Experiment>) {
    mutex.withLock {
      experimentsData.clear()
      experimentsData.putAll(experiments.map { it.name to it.variant })
    }.also { logcat { "HAnalyticsExperimentStorage:updateExperiments:$experiments" } }
  }

  override suspend fun invalidateExperiments() {
    mutex.withLock {
      experimentsData.clear()
    }.also { logcat { "HAnalyticsExperimentStorage:invalidateExperiments" } }
  }
}
