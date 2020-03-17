package com.hedvig.app.feature.language

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.hedvig.app.BaseActivity
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketModel
import com.hedvig.app.feature.marketpicker.MarketRepository
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.LanguageModel
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.makeLocaleString
import kotlinx.coroutines.launch

class LanguageAndMarketViewModel(
    private val languageRepository: LanguageRepository,
    private val marketRepository: MarketRepository,
    application: Application
) : AndroidViewModel(application) {
    val markets = MutableLiveData<List<MarketModel>>()
    private val preselectedMarket = MutableLiveData<String>()
    val isLanguageSelected = MutableLiveData<Boolean>(false)
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

    private fun isLanguageSelected() {
        var languageSelected = false
        languages.value?.let { list ->
            val selectedLanguage = list.find { it.selected }
            languageSelected = selectedLanguage?.selected ?: false
        }
        if (languageSelected) {
            isLanguageSelected.postValue(true)
        } else {
            isLanguageSelected.postValue(false)
        }
    }

    @SuppressLint("ApplySharedPref") // We want to apply this right away. It's important
    fun save() {
        val market = markets.value?.first { it.selected }?.market
        val language = languages.value?.first { it.selected }?.language
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication())

        market?.let {
            sharedPreferences.edit()
                .putInt(Market.MARKET_SHARED_PREF, market.ordinal)
                .commit()
        }

        language?.let {
            PreferenceManager
                .getDefaultSharedPreferences(getApplication())
                .edit()
                .putString(SettingsActivity.SETTING_LANGUAGE, language.toString())
                .commit()

            language.apply(getApplication())?.let { language ->
                updateLanguage(makeLocaleString(language))
            }

            LocalBroadcastManager
                .getInstance(getApplication())
                .sendBroadcast(Intent(BaseActivity.LOCALE_BROADCAST))
        }
    }

    fun selectLanguage(language: Language) {
        languages.value?.let { list ->
            for (languageModel in list) {
                languageModel.selected = languageModel.language == language
            }
        }
        isLanguageSelected()
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

        languages.value?.let { list ->
            for (languageModel in list) {
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
                languageModel.available = available
            }
        }

        languages.value?.let { list ->
            for (languageModel in list) {
                languageModel.selected = false
            }
        }
        isLanguageSelected()
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
