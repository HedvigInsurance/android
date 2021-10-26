package com.hedvig.app.feature.embark

import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.testdata.feature.embark.data.STANDARD_STORY
import org.json.JSONObject

class MockEmbarkViewModel(
    tracker: EmbarkTracker,
    graphQLQueryUseCase: GraphQLQueryUseCase
) : EmbarkViewModel(tracker, ValueStoreImpl(), graphQLQueryUseCase) {
    init {
        fetchStory("")
    }

    override fun fetchStory(name: String) {
        if (!shouldLoad) {
            return
        }
        storyData = mockedData
        setInitialState(LoginStatus.ONBOARDING)
    }

    companion object {
        var shouldLoad = true
        var mockedData = STANDARD_STORY
        var graphQLQueryResponse: JSONObject? = null
    }
}
