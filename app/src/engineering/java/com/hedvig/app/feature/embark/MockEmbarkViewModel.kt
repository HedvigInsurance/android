package com.hedvig.app.feature.embark

import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.testdata.feature.embark.data.STANDARD_STORY
import com.hedvig.hanalytics.HAnalytics

class MockEmbarkViewModel(
    tracker: EmbarkTracker,
    graphQLQueryUseCase: GraphQLQueryUseCase,
    chatRepository: ChatRepository,
    hAnalytics: HAnalytics,
    loginStatusService: LoginStatusService,
) : EmbarkViewModel(tracker, ValueStoreImpl(), graphQLQueryUseCase, chatRepository, hAnalytics, loginStatusService) {
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

    companion object {
        var shouldLoad = true
        var mockedData = STANDARD_STORY
    }
}
