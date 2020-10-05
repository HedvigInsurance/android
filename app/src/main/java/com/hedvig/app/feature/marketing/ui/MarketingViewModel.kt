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
import java.util.Stack

abstract class MarketingViewModel : ViewModel() {
    abstract val marketingBackground: LiveData<MarketingBackgroundQuery.AppMarketingImage>

    protected val _navigationState = MutableLiveData<NavigationState>()
    val navigationState: LiveData<NavigationState> = _navigationState

    private val backStack = Stack<NavigationState>()

    init {
        _navigationState.value = NavigationState(CurrentFragment.MARKET_PICKER, emptyList())
    }

    fun navigateTo(cf: CurrentFragment, vararg sharedElements: Pair<View, String>) {
        navigationState.value?.let { backStack.push(it) }
        _navigationState.postValue(NavigationState(cf, sharedElements.toList()))
    }

    fun goBack(): Boolean {
        if (backStack.empty()) {
            return false
        }
        val state = backStack.pop()
        _navigationState.postValue(state)
        return true
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

class MarketingViewModelImpl(
    marketingRepository: MarketingRepository
) : MarketingViewModel() {
    override val marketingBackground = MutableLiveData<MarketingBackgroundQuery.AppMarketingImage>()

    init {
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
