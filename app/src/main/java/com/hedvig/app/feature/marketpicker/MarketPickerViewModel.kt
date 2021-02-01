package com.hedvig.app.feature.marketpicker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.settings.Language
import kotlinx.coroutines.launch

abstract class MarketPickerViewModel : ViewModel() {
    protected val _pickerSate = MutableLiveData<PickerState>()
    val pickerState: LiveData<PickerState> = _pickerSate
    abstract fun uploadLanguage()
    abstract fun submitLanguageAndReload(market: Market?, language: Language)
}

class MarketPickerViewModelImpl(
    private val marketRepository: MarketRepository,
    private val languageRepository: LanguageRepository,
    private val localeBroadcastManager: LocaleBroadcastManager
) : MarketPickerViewModel() {

    override fun submitLanguageAndReload(market: Market?, language: Language) {
        _pickerSate.value = PickerState(market ?: pickerState.value?.market, language)
        persistPickerState()
        broadcastLocale()
        uploadLanguage()
    }

    private fun persistPickerState() {
        pickerState.value?.let { data ->
            languageRepository.persistLanguageAndMarket(
                data.language?.toString(),
                data.market?.name
            )
        }
    }

    private fun broadcastLocale() {
        localeBroadcastManager.sendBroadcast()
    }

    init {
        viewModelScope.launch {
            val market = marketRepository.getMarket()
            _pickerSate.value = PickerState(market, market.toLanguage())
        }
    }

    override fun uploadLanguage() {
        val state = pickerState.value
        state?.let {
            it.language?.let(languageRepository::uploadLanguage)
        }
    }
}

data class PickerState(
    val market: Market?,
    val language: Language?
)

sealed class Model {
    data class MarketModel(
        val selection: Market?
    ) : Model()

    data class LanguageModel(
        val selection: Language?
    ) : Model()

    object Button : Model()
}
