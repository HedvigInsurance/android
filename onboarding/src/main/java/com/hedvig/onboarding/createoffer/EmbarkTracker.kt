package com.hedvig.onboarding.createoffer

import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

abstract class EmbarkTracker {
    abstract fun track(eventName: String, properties: JSONObject? = null)
}

class EmbarkTrackerImpl(
    private val mixpanel: MixpanelAPI,
) : EmbarkTracker() {
    override fun track(eventName: String, properties: JSONObject?) = if (properties != null) {
        mixpanel.track(eventName, properties)
    } else {
        mixpanel.track(eventName)
    }
}
