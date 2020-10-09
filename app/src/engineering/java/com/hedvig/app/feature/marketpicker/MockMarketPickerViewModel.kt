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
import com.hedvig.app.util.extensions.getLanguage
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.getStoredBoolean

class MockMarketPickerViewModel(
    private val context: Context
) : MarketPickerViewModel() {

    override val data = MutableLiveData<PickerState>()

    override fun uploadLanguage() {
    }

    @SuppressLint("ApplySharedPref") // We want to apply this right away. It's important
    override fun save() {
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
        }
    }

    private fun reload() {
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent(BaseActivity.LOCALE_BROADCAST))
    }

    init {
        if (!AVAILABLE_GEO_MARKET) {
            if (context.getMarket() == null) {
                val market: Market
                try {
                    market = Market.valueOf(GEO_DATA_FI.geo.countryISOCode)
                    when (market) {
                        Market.SE -> data.postValue(PickerState(market, Language.EN_SE))
                        Market.NO -> data.postValue(PickerState(market, Language.EN_NO))
                    }
                } catch (e: Exception) {
                    data.postValue(
                        PickerState(
                            Market.SE, Language.EN_SE
                        )
                    )
                }
            } else {
                context.getMarket()?.let { _ ->
                    data.postValue(
                        PickerState(
                            Market.SE, Language.EN_SE
                        )
                    )
                }
            }
        } else {
            if (context.getMarket() == null) {
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
                context.getMarket()?.let { market ->
                    data.postValue(
                        PickerState(
                            market, context.getLanguage()
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

