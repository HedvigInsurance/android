package com.hedvig.app.feature.embark

import com.hedvig.app.testdata.feature.embark.STANDARD_STORY
import com.hedvig.app.util.jsonObjectOf
import org.json.JSONObject

class MockEmbarkViewModel : EmbarkViewModel() {
    override fun load(name: String) {
        if (!shouldLoad) {
            return
        }
        storyData =
            mockedData
        displayInitialPassage()
    }

    override suspend fun callGraphQLQuery(query: String, variables: JSONObject?) =
        jsonObjectOf("data" to graphQLQueryResponse)

    companion object {
        var shouldLoad = false
        var mockedData = STANDARD_STORY
        var graphQLQueryResponse: JSONObject? = null
    }
}
