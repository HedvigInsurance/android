package com.hedvig.app.feature.embark

import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.testdata.feature.embark.data.STANDARD_STORY

class MockEmbarkViewModel(
    tracker: EmbarkTracker,
    graphQLQueryUseCase: GraphQLQueryUseCase,
    chatRepository: ChatRepository,
    loginStatusService: LoginStatusService,
) : EmbarkViewModel(tracker, ValueStoreImpl(), graphQLQueryUseCase, chatRepository, loginStatusService) {
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
