package com.hedvig.app.feature.tracking

import com.hedvig.app.util.jsonObjectOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import java.time.LocalDateTime

class EngineeringTracker : TrackerSink {
    private val _tracks = MutableStateFlow<List<TrackEvent>>(emptyList())
    val tracks = _tracks.asStateFlow()

    override fun track(eventName: String, properties: JSONObject?) {
        _tracks.update { it + TrackEvent(eventName, properties?.toString(2), LocalDateTime.now()) }
    }

    override fun identify(id: String) {
        _tracks.update {
            it + TrackEvent(
                "identify",
                jsonObjectOf("memberId" to id).toString(2),
                LocalDateTime.now()
            )
        }
    }
}
