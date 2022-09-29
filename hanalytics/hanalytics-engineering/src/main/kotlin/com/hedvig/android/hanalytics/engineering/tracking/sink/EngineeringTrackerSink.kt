package com.hedvig.android.hanalytics.engineering.tracking.sink

import com.hedvig.android.core.common.toJsonObject
import com.hedvig.android.hanalytics.HAnalyticsSink
import com.hedvig.android.hanalytics.engineering.tracking.TrackEvent
import com.hedvig.hanalytics.HAnalyticsEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

internal class EngineeringTrackerSink : HAnalyticsSink {
  private val _tracks = MutableStateFlow<List<TrackEvent>>(emptyList())
  val tracks = _tracks.asStateFlow()

  override fun send(event: HAnalyticsEvent) {
    _tracks.update {
      listOf(
        TrackEvent(event.name, event.properties.toJsonObject().toString(2), LocalDateTime.now()),
      ) + it
    }
  }

  override fun identify() {
    _tracks.update {
      listOf(TrackEvent("identify", null, LocalDateTime.now())) + it
    }
  }

  fun clear() {
    _tracks.update { listOf() }
  }
}
