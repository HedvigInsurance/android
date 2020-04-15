package com.hedvig.app.feature.marketing.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.app.feature.marketing.data.MarketingRepository
import com.hedvig.app.util.extensions.safeLaunch

abstract class MarketingViewModel : ViewModel() {
    abstract val marketingBackground: LiveData<MarketingBackgroundQuery.AppMarketingImage>
}

class MarketingViewModelImpl(
    marketingRepository: MarketingRepository
) : MarketingViewModel() {
    override val marketingBackground = MutableLiveData<MarketingBackgroundQuery.AppMarketingImage>()

    init {
        viewModelScope.safeLaunch {
            val response = runCatching {
                marketingRepository
                    .marketingBackgroundAsync()
                    .await()
            }

            response.getOrNull()?.data()?.appMarketingImages?.firstOrNull()
                ?.let { marketingBackground.postValue(it) }
        }
    }
}
