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
        _navigationState.value = NavigationState(CurrentFragment.MARKET_PICKER)
    }

    fun navigateTo(fragment: CurrentFragment,
                   sharedElements: List<Pair<View, String>> = emptyList(),
                   reorderingAllowed: Boolean = false,
                   addToBackStack: Boolean = false) {
        _navigationState.postValue(NavigationState(fragment, sharedElements, reorderingAllowed, addToBackStack))
    }
}

class MarketingViewModelImpl(
    marketingRepository: MarketingRepository,
    context: Context
) : MarketingViewModel() {
    override val marketingBackground = MutableLiveData<MarketingBackgroundQuery.AppMarketingImage>()

    init {
        if (context.getStoredBoolean(MarketingActivity.SHOULD_OPEN_MARKET_SELECTED)) {
            _navigationState.value = NavigationState(CurrentFragment.MARKETING)
        } else {
            _navigationState.value = NavigationState(CurrentFragment.MARKET_PICKER)
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
    val addToBackStack: Boolean = false
)
