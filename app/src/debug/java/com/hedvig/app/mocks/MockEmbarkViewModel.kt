package com.hedvig.app.mocks

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.EmbarkViewModel

class MockEmbarkViewModel : EmbarkViewModel() {
    override val data = MutableLiveData<EmbarkStoryQuery.Data>()

    override fun load(name: String) {
        data.postValue(
            EmbarkStoryQuery.Data(
                embarkStory = EmbarkStoryQuery.EmbarkStory(
                    startPassage = "1",
                    passages = listOf(
                        EmbarkStoryQuery.Passage(
                            id = "1",
                            messages = listOf(
                                EmbarkStoryQuery.Message(
                                    text = "test message"
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
