package com.hedvig.app.mocks

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.MarketingStoriesQuery
import com.hedvig.app.feature.marketing.ui.MarketingStoriesViewModel

class MockMarketingStoriesViewModel : MarketingStoriesViewModel() {
    override val marketingStories = MutableLiveData<List<MarketingStoriesQuery.MarketingStory>>()

    init {
        marketingStories.postValue(
            listOf(
                MarketingStoriesQuery.MarketingStory(
                    id = "123",
                    asset = MarketingStoriesQuery.Asset(
                        mimeType = "video/mp4",
                        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
                    ),
                    duration = 4.0
                )
            )
        )
    }
}
