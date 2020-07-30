package com.hedvig.app.feature.embark

import com.hedvig.app.testdata.feature.embark.STANDARD_STORY

class MockEmbarkViewModel : EmbarkViewModel() {
    override fun load(name: String) {
        if (!shouldLoad) {
            return
        }
        storyData =
            mockedData
        displayInitialPassage()
    }

    companion object {
        var shouldLoad = false
        var mockedData = STANDARD_STORY
    }
}
