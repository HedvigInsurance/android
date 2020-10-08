package com.hedvig.app.feature.marketpicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.hedvig.app.BaseActivity
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_FI
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_SE

class MockMarketPickerViewModel(
    private val context: Context
) : MarketPickerViewModel() {

    override var dirty = false

    override val data = MutableLiveData<PickerState>()

    override fun uploadLanguage() {
    }

    @SuppressLint("ApplySharedPref") // We want to apply this right away. It's important
    fun save() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

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

            reload()

            dirty = true
        }
    }

    private fun reload() {
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent(BaseActivity.LOCALE_BROADCAST))
    }

    override fun saveIfNotDirty() {
        if (!dirty) {
            save()
        }
    }

    init {
        if (!AVAILABLE_GEO_MARKET) {
            if (MockMarketProvider().market == null) {
                val market: Market
                try {
                    market = Market.valueOf(GEO_DATA_FI.geo.countryISOCode)
                    when (market) {
                        Market.SE -> data.postValue(PickerState(market, Language.SV_SE))
                        Market.NO -> data.postValue(PickerState(market, Language.NB_NO))
                    }
                } catch (e: Exception) {
                    data.postValue(
                        PickerState(
                            Market.SE, Language.EN_SE
                        )
                    )
                }
            } else {
                MockMarketProvider().market?.let { _ ->
                    data.postValue(
                        PickerState(
                            Market.SE, Language.EN_SE
                        )
                    )
                }
            }
        } else {
            if (MockMarketProvider().market == null) {
                val market: Market
                try {
                    market = Market.valueOf(GEO_DATA_SE.geo.countryISOCode)
                    when (market) {
                        Market.SE -> data.postValue(PickerState(market, Language.SV_SE))
                        Market.NO -> data.postValue(PickerState(market, Language.NB_NO))
                    }
                } catch (e: Exception) {
                    data.postValue(
                        PickerState(
                            Market.SE, Language.EN_SE
                        )
                    )
                }
            } else {
                MockMarketProvider().market?.let { market ->
                    data.postValue(
                        PickerState(
                            market, Language.EN_SE
                        )
                    )
                }
            }
        }
    }

    companion object {
        var AVAILABLE_GEO_MARKET = true
    }
}

class MockMarketProvider : MarketProvider() {
    override val market: Market? = null
}
