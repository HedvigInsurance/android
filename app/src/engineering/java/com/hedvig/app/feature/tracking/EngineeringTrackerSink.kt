package com.hedvig.app.feature.tracking

import com.hedvig.app.feature.hanalytics.HAnalyticsSink
import com.hedvig.app.util.toJsonObject
import com.hedvig.hanalytics.HAnalyticsEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

class EngineeringTrackerSink : HAnalyticsSink {
    private val _tracks = MutableStateFlow<List<TrackEvent>>(emptyList())
    val tracks = _tracks.asStateFlow()

    override fun send(event: HAnalyticsEvent) {
        _tracks.update {
            listOf(
                TrackEvent(event.name, event.properties.toJsonObject().toString(2), LocalDateTime.now())
            ) + it
        }
    }

    fun clear() {
        _tracks.update { listOf() }
    }
}
