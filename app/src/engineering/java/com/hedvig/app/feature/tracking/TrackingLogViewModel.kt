package com.hedvig.app.feature.tracking

import androidx.lifecycle.ViewModel

class TrackingLogViewModel(
    engineeringTracker: EngineeringTracker
) : ViewModel() {
    val tracks = engineeringTracker.tracks
}
