package com.hedvig.app.feature.embark

import com.hedvig.android.auth.AuthTokenService
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.testdata.feature.embark.data.STANDARD_STORY
import com.hedvig.hanalytics.HAnalytics

class MockEmbarkViewModel(
  graphQLQueryUseCase: GraphQLQueryUseCase,
  chatRepository: ChatRepository,
  hAnalytics: HAnalytics,
  authTokenService: AuthTokenService,
) : EmbarkViewModel(
  ValueStoreImpl(),
  graphQLQueryUseCase,
  chatRepository,
  hAnalytics,
  "",
  authTokenService,
) {
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
