package com.hedvig.app.feature.marketpicker

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.hedvig.app.BaseActivity
import com.hedvig.app.HedvigApplication
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.makeLocaleString
import com.hedvig.app.util.extensions.getLanguage
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.getStoredBoolean
import kotlinx.coroutines.launch

abstract class MarketPickerViewModel(application: Application) : AndroidViewModel(application) {
    protected open val _data = MutableLiveData<PickerState>()
    open val data: LiveData<PickerState> = _data
    abstract fun uploadLanguage()

    @SuppressLint("ApplySharedPref")// We want to apply this right away. It's important
    fun save() {
        val application = getApplication<HedvigApplication>()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        var clean = false
        data.value?.let { ps ->
            clean = ps.market == application.getMarket() && ps.language == application.getLanguage()
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

            if (!clean || application.getStoredBoolean(MarketPickerFragment.SHOULD_PROCEED)) {
                reload()
            }
            uploadLanguage()
        }
    }

    private fun reload() {
        LocalBroadcastManager
            .getInstance(getApplication<HedvigApplication>())
            .sendBroadcast(Intent(BaseActivity.LOCALE_BROADCAST))
    }

    fun updatePickerState(pickerState: PickerState?) {
        _data.postValue(pickerState)
    }
}

class MarketPickerViewModelImpl(
    private val marketRepository: MarketRepository,
    private val languageRepository: LanguageRepository,
    private val context: Context,
    application: Application
) : MarketPickerViewModel(application) {
    override val _data = MutableLiveData<PickerState>()
    override val data: LiveData<PickerState> = _data

    init {
        viewModelScope.launch {
            if (context.getMarket() == null) {
                val geo = runCatching { marketRepository.geoAsync().await() }
                geo.getOrNull()?.data?.let {
                    runCatching {
                        val market: Market
                        try {
                            market = Market.valueOf(it.geo.countryISOCode)
                            when (market) {
                                Market.SE -> _data.postValue(PickerState(market, Language.EN_SE))
                                Market.NO -> _data.postValue(PickerState(market, Language.EN_NO))
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
                            market, context.getLanguage()
                        )
                    )
                }
            }
        }
    }

    override fun uploadLanguage() {
        data.value?.let { data ->
            data.language?.apply(context)?.let { language ->
                languageRepository.setLanguage(makeLocaleString(language))
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

