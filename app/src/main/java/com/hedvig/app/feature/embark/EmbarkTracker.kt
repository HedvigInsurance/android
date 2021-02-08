package com.hedvig.app.feature.embark

import com.mixpanel.android.mpmetrics.MixpanelAPI

class EmbarkTracker(
    private val mixpanel: MixpanelAPI,
) {
    fun track(eventName: String) = mixpanel.track(eventName)
}
