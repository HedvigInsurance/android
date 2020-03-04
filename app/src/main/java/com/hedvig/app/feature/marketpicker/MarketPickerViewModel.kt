package com.hedvig.app.feature.marketpicker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class MarketPickerViewModel : ViewModel() {
    abstract val selectedMarket: LiveData<Market>
    abstract val preselectedMarket: LiveData<String>
    abstract fun updateMarket(market: Market)
    abstract fun loadGeo()
}

class MarketPickerViewModelImpl(private val repository: MarketRepository) :
    MarketPickerViewModel() {
    override val selectedMarket = MutableLiveData<Market>()
    override val preselectedMarket = MutableLiveData<String>()

    override fun updateMarket(market: Market) {
        selectedMarket.postValue(market)
    }

    override fun loadGeo() {
        viewModelScope.launch {
            repository.geo()
                .collect { response ->
                    preselectedMarket.postValue(response.data()?.geo?.countryISOCode)
                }
        }
    }
}
