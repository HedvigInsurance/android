package com.hedvig.app.feature.language

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import com.hedvig.app.util.apollo.defaultLocale
import e
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LanguageAndMarketViewModel(
    private val languageRepository: LanguageRepository,
    private val marketRepository: MarketRepository,
    private val context: Context
) : ViewModel() {
    val markets = MutableLiveData<List<MarketModel>>()
    val preselectedMarket = MutableLiveData<String>()
    val isLanguageSelected = MutableLiveData(false)
    val languages = MutableLiveData<List<LanguageModel>>()

    init {
        markets.value = Market.values().map { market ->
            MarketModel(
                market
            )
        }

        languages.value = Language.values().map { language ->
            LanguageModel(
                language
            )
        }
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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        market?.let {
            sharedPreferences.edit()
                .putString(Market.MARKET_SHARED_PREF, market.name)
                .commit()

        }

        language?.let {
            PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(SettingsActivity.SETTING_LANGUAGE, language.toString())
                .commit()

            language.apply(context)?.let { language ->
                updateLanguage(makeLocaleString(language))

                viewModelScope.launch {
                    withContext(NonCancellable) {
                        runCatching {
                            marketRepository
                                .updatePickedLocaleAsync(defaultLocale(language))
                        }
                    }
                }
            }

            LocalBroadcastManager
                .getInstance(context)
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
            val response = runCatching {
                marketRepository.geoAsync().await()
            }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                updateMarket(Market.SE)
            } else {
                preselectedMarket.postValue(response.getOrNull()?.data()?.geo?.countryISOCode)
                response.getOrNull()?.data()?.geo?.let { geo ->
                    val market: Market = try {
                        Market.valueOf(geo.countryISOCode)
                    } catch (e: Exception) {
                        Market.SE
                    }
                    updateMarket(market)
                }
            }
        }
    }
}
