package com.hedvig.app.feature.marketpicker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class MarketPickerViewModel : ViewModel() {
    abstract val selectedMarket: LiveData<String>
    abstract fun updateMarket(market: String)
}

class MarketPickerViewModelImpl : MarketPickerViewModel() {
    override val selectedMarket = MutableLiveData<String>()

    override fun updateMarket(market: String) {
        selectedMarket.postValue(market)
    }
}
