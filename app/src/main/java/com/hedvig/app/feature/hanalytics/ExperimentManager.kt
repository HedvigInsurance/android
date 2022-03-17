package com.hedvig.app.feature.hanalytics

import com.hedvig.hanalytics.HAnalyticsEvent
import com.hedvig.hanalytics.HAnalyticsExperiment
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface ExperimentManager {
    suspend fun getExperiment(name: String): HAnalyticsExperiment
    suspend fun preloadExperiments()
    suspend fun invalidateExperiments()
}

class ExperimentManagerImpl(
    private val sendHAnalyticsEventUseCase: SendHAnalyticsEventUseCase,
    private val hAnalyticsService: HAnalyticsService,
) : ExperimentManager {
    private val mutex = Mutex()
    private val experimentsData = mutableMapOf<String, String>()

    override suspend fun getExperiment(name: String): HAnalyticsExperiment {
        return mutex.withLock {
            if (experimentsData.isEmpty()) {
                loadExperimentsFromServer()
            }

            experimentsData[name]?.let { variant ->
                HAnalyticsExperiment(name, variant)
            } ?: throw Exception("experiment unavailable")
        }
    }

    override suspend fun preloadExperiments() {
        mutex.withLock {
            loadExperimentsFromServer()
        }
    }

    override suspend fun invalidateExperiments() {
        mutex.withLock {
            experimentsData.clear()
        }
    }

    private suspend fun loadExperimentsFromServer() {
        val experimentsList = hAnalyticsService.getExperiments()
        sendExperimentsLoadedEvent(experimentsList)
        if (experimentsList == null) return
        experimentsData.clear()
        experimentsData.putAll(experimentsList.map { it.name to it.variant })
    }

    private fun sendExperimentsLoadedEvent(experimentsList: List<Experiment>?) {
        val experimentsLoadedEvent = HAnalyticsEvent(
            "experiments_loaded",
            mapOf("experiments" to experimentsList),
        )
        sendHAnalyticsEventUseCase.send(experimentsLoadedEvent)
    }
}
