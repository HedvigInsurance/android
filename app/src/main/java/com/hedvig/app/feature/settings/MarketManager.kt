package com.hedvig.app.feature.settings

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.hedvig.app.isDebug

interface MarketManager {
    val enabledMarkets: List<Market>
    var market: Market?
    var hasSelectedMarket: Boolean
}

class MarketManagerImpl(
    context: Context
) : MarketManager {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override val enabledMarkets = listOfNotNull(
        Market.SE,
        Market.NO,
        Market.DK,
        if (isDebug()) Market.FR else null
    )

    override var market: Market?
        get() = getMarketLocally()
        set(value) {
            value?.let(::setMarketLocally) ?: removeMarket()
        }
    override var hasSelectedMarket: Boolean
        get() {
            return sharedPreferences.getBoolean("HAS_SELECTED_MARKET", false)
        }
        set(value) {
            sharedPreferences.edit {
                putBoolean("HAS_SELECTED_MARKET", value)
            }
        }

    private fun getMarketLocally(): Market? {
        return sharedPreferences
            .getString(Market.MARKET_SHARED_PREF, null)
            ?.let { Market.valueOf(it) }
    }

    @SuppressLint("ApplySharedPref") // We need to do this right away
    private fun setMarketLocally(market: Market) {
        sharedPreferences.edit()
            .putString(Market.MARKET_SHARED_PREF, market.name)
            .commit()
    }

    @SuppressLint("ApplySharedPref") // We need to do this right away
    private fun removeMarket() {
        sharedPreferences.edit()
            .remove(Market.MARKET_SHARED_PREF)
            .commit()
    }
}
