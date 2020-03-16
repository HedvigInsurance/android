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
import com.hedvig.app.feature.settings.LanguageModel
import kotlinx.coroutines.launch

class LanguageAndMarketViewModel(
    private val languageRepository: LanguageRepository,
    private val marketRepository: MarketRepository,
    application: Application
) : AndroidViewModel(application) {
    val markets = MutableLiveData<List<MarketModel>>()
    val preselectedMarket = MutableLiveData<String>()
    val isMarketAndLanguageSelected = MutableLiveData<Boolean>(false)
    val languages = MutableLiveData<List<LanguageModel>>()

    init {
        markets.postValue(Market.values().map { market ->
            MarketModel(
                market
            )
        })

        languages.postValue(Language.values().map { language ->
            LanguageModel(
                language
            )
        })
    }

    private fun setMarketAndLanguageSelected() {
        val selectedMarket = markets.value?.firstOrNull { it.selected }
        val selectedLanguage = languages.value?.firstOrNull { it.selected }
        if (selectedMarket != null && selectedLanguage != null) {
            isMarketAndLanguageSelected.postValue(true)
        } else {
            isMarketAndLanguageSelected.postValue(false)
        }
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
        languages.postValue(languages.value?.map { languageModel ->
            LanguageModel(
                languageModel.language,
                languageModel.language == language,
                languageModel.available
            )
        })
    }

    fun updateLanguage(acceptLanguage: String) {
        languageRepository
            .setLanguage(acceptLanguage)
    }

    fun updateMarket(market: Market) {
        markets.postValue(markets.value?.map { marketModel ->
            MarketModel(
                marketModel.market,
                marketModel.market == market
            )
        })

        languages.postValue(languages.value?.map { languageModel ->
            var available = false
            if (market == Market.SE) {
                if (languageModel.language == Language.SV_SE || languageModel.language == Language.EN_SE) {
                    available = true
                }
            } else if (market == Market.NO) {
                if (languageModel.language == Language.NB_NO || languageModel.language == Language.EN_NO) {
                    available = true
                }
            }
            LanguageModel(
                languageModel.language,
                languageModel.selected,
                available
            )
        })
        languages.value?.let { list ->
            for (languageModel in list) {
                languageModel.selected = false
            }
        }
    }

    fun loadGeo() {
        viewModelScope.launch {
            val response = marketRepository.geoAsync().await()
            preselectedMarket.postValue(response.data()?.geo?.countryISOCode)
            response.data()?.geo?.let { geo ->
                updateMarket(Market.valueOf(geo.countryISOCode))
            }
        }
    }
}
