package com.hedvig.app.util.featureflags.flags

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DebugFeatureFlagProvider(
    private val dataStore: DataStore<Preferences>,
    private val marketManager: MarketManager,
) : FeatureFlagProvider {

    override suspend fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.EXTERNAL_DATA_COLLECTION -> isEnabled(Feature.EXTERNAL_DATA_COLLECTION, defaultValue = false)
        Feature.FRANCE_MARKET -> isEnabled(Feature.FRANCE_MARKET, defaultValue = true)
        Feature.KEY_GEAR -> isEnabled(Feature.KEY_GEAR, defaultValue = false)
        Feature.MOVING_FLOW -> isEnabled(Feature.MOVING_FLOW, defaultValue = true)
        Feature.CONNECT_PAYMENT_AT_SIGN -> {
            (marketManager.market == Market.NO || marketManager.market == Market.DK) &&
                isEnabled(Feature.CONNECT_PAYMENT_AT_SIGN, defaultValue = true)
        }
        Feature.UPDATE_NECESSARY -> isEnabled(Feature.UPDATE_NECESSARY, defaultValue = false)
        Feature.REFERRAL_CAMPAIGN -> isEnabled(Feature.REFERRAL_CAMPAIGN, defaultValue = false)
        Feature.QUOTE_CART -> isEnabled(Feature.QUOTE_CART, defaultValue = false)
    }

    private suspend fun isEnabled(feature: Feature, defaultValue: Boolean): Boolean {
        return dataStore.data.map { it[booleanPreferencesKey(feature.name)] }.firstOrNull() ?: defaultValue
    }
}
