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
                            name = "TestPassage",
                            id = "1",
                            messages = listOf(
                                EmbarkStoryQuery.Message(
                                    text = "test message"
                                ),
                                EmbarkStoryQuery.Message(
                                    text = "123"
                                )
                            ),
                            action = EmbarkStoryQuery.Action(
                                asEmbarkSelectAction = EmbarkStoryQuery.AsEmbarkSelectAction(
                                    data = EmbarkStoryQuery.Data1(
                                        options = listOf(
                                            EmbarkStoryQuery.Option(
                                                link = EmbarkStoryQuery.Link(
                                                    name = "TestPassage",
                                                    label = "Test select action"
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
