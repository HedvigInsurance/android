package com.hedvig.app.feature.language

import android.widget.RadioButton
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketRepository
import com.hedvig.app.feature.settings.Language
import kotlinx.coroutines.launch

class LanguageAndMarketViewModel(
    private val languageRepository: LanguageRepository,
    private val marketRepository: MarketRepository
) : ViewModel() {
    val selectedLanguage = MutableLiveData<Language>()
    val marketLastCheckedPos = MutableLiveData(0)
    val marketLastChecked = MutableLiveData<RadioButton>(null)
    val languageLastCheckedPos = MutableLiveData(0)
    val languageLastChecked = MutableLiveData<RadioButton>(null)

    fun selectLanguage(language: Language) {
        selectedLanguage.postValue(language)
    }

    fun updateLanguage(acceptLanguage: String) {
        languageRepository
            .setLanguage(acceptLanguage)
    }

    val selectedMarket = MutableLiveData<Market>()
    val preselectedMarket = MutableLiveData<String>()

    fun updateMarket(market: Market) {
        selectedMarket.postValue(market)
    }

    fun loadGeo() {
        viewModelScope.launch {
            val response = marketRepository.geoAsync().await()
            preselectedMarket.postValue(response.data()?.geo?.countryISOCode)

        }
    }
}
