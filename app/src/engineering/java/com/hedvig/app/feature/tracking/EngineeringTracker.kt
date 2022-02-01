package com.hedvig.app.feature.tracking

import com.hedvig.app.util.toJsonObject
import com.hedvig.hanalytics.HAnalyticsEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

class EngineeringTracker : HAnalyticsSink {
    private val _tracks = MutableStateFlow<List<TrackEvent>>(emptyList())
    val tracks = _tracks.asStateFlow()

    override fun send(event: HAnalyticsEvent) {
        _tracks.update { it + TrackEvent(event.name, event.properties.toJsonObject().toString(2), LocalDateTime.now()) }
    }
}
