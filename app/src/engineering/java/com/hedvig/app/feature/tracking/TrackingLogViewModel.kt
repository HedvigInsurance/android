package com.hedvig.app.feature.tracking

import androidx.lifecycle.ViewModel

class TrackingLogViewModel(
    private val engineeringTrackerSink: EngineeringTrackerSink,
) : ViewModel() {
    val tracks = engineeringTrackerSink.tracks

    fun clear() = engineeringTrackerSink.clear()
}
