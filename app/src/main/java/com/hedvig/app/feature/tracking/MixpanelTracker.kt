package com.hedvig.app.feature.tracking

import com.hedvig.app.util.jsonObjectOf
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

class MixpanelTracker(
    private val mixpanel: MixpanelAPI
) : TrackerSink {
    override fun track(eventName: String, properties: JSONObject?) {
        if (properties != null) {
            mixpanel.track(eventName, properties)
        } else {
            mixpanel.track(eventName)
        }
    }

    override fun identify(id: String) {
        mixpanel.identify(id)
    }

    override fun setProperty(name: String, value: String) {
        mixpanel.registerSuperProperties(jsonObjectOf(name to value))
    }
}
