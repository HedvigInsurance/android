package com.hedvig.app.feature.embark

import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.feature.chat.usecase.TriggerFreeTextChatUseCase
import com.hedvig.app.testdata.feature.embark.data.STANDARD_STORY

class MockEmbarkViewModel(
    tracker: EmbarkTracker,
    graphQLQueryUseCase: GraphQLQueryUseCase,
    triggerFreeTextChatUseCase: TriggerFreeTextChatUseCase,
) : EmbarkViewModel(tracker, ValueStoreImpl(), graphQLQueryUseCase, triggerFreeTextChatUseCase) {
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
    }
}
