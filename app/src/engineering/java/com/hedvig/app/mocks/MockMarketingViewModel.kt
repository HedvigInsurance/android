package com.hedvig.app.mocks

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.app.feature.marketing.ui.MarketingViewModel
import com.hedvig.app.feature.tracking.MockHAnalytics

class MockMarketingViewModel : MarketingViewModel(MockHAnalytics()) {
    override val marketingBackground = MutableLiveData<MarketingBackgroundQuery.AppMarketingImage>()

    init {
    }
}
