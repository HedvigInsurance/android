package com.hedvig.app.feature.tracking

import androidx.lifecycle.ViewModel

class TrackingLogViewModel(
    private val engineeringTracker: EngineeringTracker
) : ViewModel() {
    val tracks = engineeringTracker.tracks

    fun clear() = engineeringTracker.clear()
}
