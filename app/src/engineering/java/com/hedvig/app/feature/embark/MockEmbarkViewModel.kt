package com.hedvig.app.feature.embark

import com.hedvig.app.testdata.feature.embark.data.STANDARD_STORY
import com.hedvig.app.util.jsonObjectOf
import org.json.JSONObject

class MockEmbarkViewModel(tracker: EmbarkTracker) : EmbarkViewModel(tracker, ValueStoreImpl()) {
    init {
        fetchStory("")
    }

    override fun fetchStory(name: String) {
        if (!shouldLoad) {
            return
        }
        storyData = mockedData
        setInitialState()
    }

    override suspend fun callGraphQL(query: String, variables: JSONObject?) =
        jsonObjectOf("data" to graphQLQueryResponse)

    companion object {
        var shouldLoad = true
        var mockedData = STANDARD_STORY
        var graphQLQueryResponse: JSONObject? = null
    }
}
