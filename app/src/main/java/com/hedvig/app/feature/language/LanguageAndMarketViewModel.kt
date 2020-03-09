package com.hedvig.app.feature.language

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketModel
import com.hedvig.app.feature.marketpicker.MarketRepository
import com.hedvig.app.feature.settings.Language
import kotlinx.coroutines.launch

class LanguageAndMarketViewModel(
    private val languageRepository: LanguageRepository,
    private val marketRepository: MarketRepository,
    application: Application
) : AndroidViewModel(application) {
    val selectedLanguage = MutableLiveData<Language>()
    val markets = MutableLiveData<List<MarketModel>>()

    init {
        markets.postValue(Market.values().map { market ->
            MarketModel(
                market
            )
        })
    }

    fun save() {
        val selected = markets.value?.first { it.selected }?.market
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication())

        selected?.let {
            sharedPreferences.edit()
                .putInt(Market.MARKET_SHARED_PREF, selected.ordinal)
                .commit()
        }
    }

    fun selectLanguage(language: Language) {
        selectedLanguage.postValue(language)
    }

    fun updateLanguage(acceptLanguage: String) {
        languageRepository
            .setLanguage(acceptLanguage)
    }

    val preselectedMarket = MutableLiveData<String>()

    fun updateMarket(market: Market) {
        markets.postValue(markets.value?.map { marketModel ->
            MarketModel(
                marketModel.market,
                marketModel.market == market
            )
        })
    }

    fun loadGeo() {
        viewModelScope.launch {
            val response = marketRepository.geoAsync().await()
            preselectedMarket.postValue(response.data()?.geo?.countryISOCode)

        }
    }
}
