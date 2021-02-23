package com.hedvig.app.feature.marketing.ui

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.app.feature.marketing.data.MarketingRepository
import com.hedvig.app.feature.marketpicker.CurrentFragment
import kotlinx.coroutines.launch

abstract class MarketingViewModel : ViewModel() {
    abstract val marketingBackground: LiveData<MarketingBackgroundQuery.AppMarketingImage>

    protected val _navigationState = MutableLiveData<NavigationState>()
    val navigationState: LiveData<NavigationState> = _navigationState

    init {
        _navigationState.value = NavigationState(CurrentFragment.MARKET_PICKER)
    }

    fun navigateTo(navigationState: NavigationState) {
        _navigationState.postValue(navigationState)
    }
}

class MarketingViewModelImpl(marketingRepository: MarketingRepository) : MarketingViewModel() {
    override val marketingBackground = MutableLiveData<MarketingBackgroundQuery.AppMarketingImage>()

    init {
        _navigationState.value = if (marketingRepository.hasSelectedMarket()) {
            NavigationState(CurrentFragment.MARKETING)
        } else {
            NavigationState(CurrentFragment.MARKET_PICKER)
        }

        viewModelScope.launch {
            runCatching { marketingRepository.marketingBackground() }
                .getOrNull()
                ?.data
                ?.appMarketingImages
                ?.firstOrNull()
                ?.let(marketingBackground::postValue)
        }
    }
}

data class NavigationState(
    val destination: CurrentFragment,
    val sharedElements: List<Pair<View, String>> = emptyList(),
    val reorderingAllowed: Boolean = false,
    val addToBackStack: Boolean = false,
    val recreate: Boolean = false
)
