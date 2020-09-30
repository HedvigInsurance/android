package com.hedvig.app.feature.marketpicker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.settings.Language
import kotlinx.coroutines.launch

class MarketPickerViewModel(
    private val marketRepository: MarketRepository
) : ViewModel() {
    val data = MutableLiveData<PickerState>()

    init {
        viewModelScope.launch {
            val geo = runCatching { marketRepository.geoAsync().await() }
            geo.getOrNull()?.data?.let {
                runCatching {
                    when (val market = Market.valueOf(it.geo.countryISOCode)) {
                        Market.SE -> data.postValue(PickerState(market, Language.SV_SE))
                        Market.NO -> data.postValue(PickerState(market, Language.NB_NO))
                    }
                }
            }
        }
    }
}

data class PickerState(
    val market: Market?,
    val language: Language?
)

sealed class Model {
    object Title : Model()
    data class MarketModel(
        val selection: Market?
    ) : Model()

    data class LanguageModel(
        val selection: Language?
    ) : Model()

    object Button : Model()
}
