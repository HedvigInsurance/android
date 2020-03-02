package com.hedvig.app.feature.marketpicker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class MarketPickerViewModel : ViewModel() {
    abstract val selectedMarket: LiveData<Country>
    abstract fun updateMarket(market: Country)
}

class MarketPickerViewModelImpl : MarketPickerViewModel() {
    override val selectedMarket = MutableLiveData<Country>()

    override fun updateMarket(market: Country) {
        selectedMarket.postValue(market)
    }
}
