package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import org.json.JSONObject

data class TrackBuilder(
    private val eventName: String,
    private val keys: List<String> = emptyList(),
    private val includeAllKeys: Boolean = false,
    private val customData: JSONObject? = null,
) {
    fun build() = EmbarkStoryQuery.Track(
        eventName = eventName,
        eventKeys = keys,
        includeAllKeys = includeAllKeys,
        customData = customData,
    )
}
