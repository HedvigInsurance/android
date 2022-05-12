package com.hedvig.app.feature.marketing.ui

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.app.feature.marketing.data.MarketingRepository
import com.hedvig.app.feature.marketpicker.CurrentFragment
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch

abstract class MarketingViewModel(
    private val hAnalytics: HAnalytics,
) : ViewModel() {
    abstract val marketingBackground: LiveData<MarketingBackgroundQuery.AppMarketingImage>

    protected val _navigationState = MutableLiveData<NavigationState>()
    val navigationState: LiveData<NavigationState> = _navigationState

    init {
        _navigationState.value = NavigationState(CurrentFragment.MARKET_PICKER)
        hAnalytics.screenViewMarketPicker()
    }

    fun navigateTo(navigationState: NavigationState) {
        when (navigationState.destination) {
            CurrentFragment.MARKET_PICKER -> {
                hAnalytics.screenViewMarketPicker()
            }
            CurrentFragment.MARKETING -> {
                hAnalytics.screenViewMarketing()
            }
        }
        _navigationState.postValue(navigationState)
    }

    fun onClickSignUp() {
        hAnalytics.buttonClickMarketingOnboard()
    }

    fun onClickLogin() {
        hAnalytics.buttonClickMarketingLogin()
    }
}

class MarketingViewModelImpl(
    marketingRepository: MarketingRepository,
    marketManager: MarketManager,
    hAnalytics: HAnalytics,
) : MarketingViewModel(hAnalytics) {

    override val marketingBackground = MutableLiveData<MarketingBackgroundQuery.AppMarketingImage>()

    init {
        _navigationState.value = if (marketManager.hasSelectedMarket) {
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
