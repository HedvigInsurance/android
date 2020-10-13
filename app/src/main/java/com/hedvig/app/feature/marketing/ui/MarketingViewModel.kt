package com.hedvig.app.feature.marketing.ui

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.app.feature.marketing.data.MarketingRepository
import com.hedvig.app.feature.marketpicker.CurrentFragment
import com.hedvig.app.util.extensions.getStoredBoolean
import kotlinx.coroutines.launch

abstract class MarketingViewModel : ViewModel() {
    abstract val marketingBackground: LiveData<MarketingBackgroundQuery.AppMarketingImage>

    protected val _navigationState = MutableLiveData<NavigationState>()
    val navigationState: LiveData<NavigationState> = _navigationState

    init {
        _navigationState.value = NavigationState(CurrentFragment.MARKET_PICKER, emptyList())
    }

    fun navigateTo(cf: CurrentFragment, vararg sharedElements: Pair<View, String>) {
        _navigationState.postValue(NavigationState(cf, sharedElements.toList()))
    }
}

class MarketingViewModelImpl(
    marketingRepository: MarketingRepository,
    context: Context
) : MarketingViewModel() {
    override val marketingBackground = MutableLiveData<MarketingBackgroundQuery.AppMarketingImage>()

    init {
        if (context.getStoredBoolean(MarketingActivity.SHOULD_OPEN_MARKET_SELECTED)) {
            _navigationState.value = NavigationState(CurrentFragment.MARKETING, emptyList())
        } else {
            _navigationState.value = NavigationState(CurrentFragment.MARKET_PICKER, emptyList())
        }
        viewModelScope.launch {
            val response = runCatching {
                marketingRepository
                    .marketingBackgroundAsync()
                    .await()
            }

            response.getOrNull()?.data?.appMarketingImages?.firstOrNull()
                ?.let { marketingBackground.postValue(it) }
        }
    }
}

data class NavigationState(
    val destination: CurrentFragment,
    private var _sharedElements: List<Pair<View, String>>
) {
    val sharedElements: List<Pair<View, String>>
        get() {
            val elems = _sharedElements.toMutableList()
            _sharedElements = emptyList()
            return elems
        }
}
