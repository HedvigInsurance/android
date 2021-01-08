package com.hedvig.app.feature.marketpicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.BaseActivity
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.makeLocaleString
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.extensions.getLanguage
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.getStoredBoolean
import kotlinx.coroutines.launch

abstract class MarketPickerViewModel(private val context: Context) : ViewModel() {
    protected val _data = MutableLiveData<PickerState>()
    val data: LiveData<PickerState> = _data
    abstract fun uploadLanguage()

    @SuppressLint("ApplySharedPref")// We want to apply this right away. It's important
    fun save() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var clean = false
        data.value?.let { ps ->
            clean = ps.market == context.getMarket() && ps.language == context.getLanguage()
        }

        data.value?.let { data ->
            sharedPreferences.edit()
                .putString(
                    Market.MARKET_SHARED_PREF,
                    data.market?.name
                )
                .commit()

            sharedPreferences
                .edit()
                .putString(SettingsActivity.SETTING_LANGUAGE, data.language.toString())
                .commit()

            if (!clean || context.getStoredBoolean(MarketPickerFragment.SHOULD_PROCEED)) {
                reload()
            }
            uploadLanguage()
        }
    }

    private fun reload() {
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent(BaseActivity.LOCALE_BROADCAST))
    }

    fun updatePickerState(pickerState: PickerState?) {
        _data.value = pickerState
    }
}

class MarketPickerViewModelImpl(
    private val marketRepository: MarketRepository,
    private val languageRepository: LanguageRepository,
    private val context: Context
) : MarketPickerViewModel(context) {

    init {
        viewModelScope.launch {
            if (context.getMarket() == null) {
                val geo = runCatching { marketRepository.geo() }
                if (geo.isFailure || geo.getOrNull()?.hasErrors() == true) {
                    _data.postValue(
                        PickerState(
                            Market.SE, Language.EN_SE
                        )
                    )
                    return@launch
                }
                geo.getOrNull()?.data?.let {
                    runCatching {
                        val market: Market
                        try {
                            market = Market.valueOf(it.geo.countryISOCode)
                            when (market) {
                                Market.SE -> _data.postValue(PickerState(market, Language.EN_SE))
                                Market.NO -> _data.postValue(PickerState(market, Language.EN_NO))
                                Market.DK -> _data.postValue(PickerState(market, Language.EN_DK))
                            }
                        } catch (e: Exception) {
                            _data.postValue(
                                PickerState(
                                    Market.SE, Language.EN_SE
                                )
                            )
                        }

                    }
                }
            } else {
                context.getMarket()?.let { market ->
                    _data.postValue(
                        PickerState(
                            market,
                            context.getLanguage() ?: Language.getAvailableLanguages(market).first()
                        )
                    )
                }
            }
        }
    }

    override fun uploadLanguage() {
        data.value?.let { data ->
            data.language?.apply(context)?.let { ctx ->
                languageRepository.setLanguage(makeLocaleString(ctx), defaultLocale(ctx))
            }
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

