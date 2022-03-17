package com.hedvig.app.util.featureflags

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class DebugFeatureFlagProvider(
    private val dataStore: DataStore<Preferences>,
) : FeatureFlagProvider {

    override val priority = DEBUG_PRIORITY

    override suspend fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> isEnabled(Feature.MOVING_FLOW, defaultValue = true)
        Feature.FRANCE_MARKET -> isEnabled(Feature.FRANCE_MARKET, defaultValue = true)
        Feature.ADDRESS_AUTO_COMPLETE -> isEnabled(Feature.ADDRESS_AUTO_COMPLETE, defaultValue = true)
        Feature.REFERRAL_CAMPAIGN -> isEnabled(Feature.REFERRAL_CAMPAIGN, defaultValue = false)
        Feature.QUOTE_CART -> isEnabled(Feature.QUOTE_CART, defaultValue = false)
        Feature.HEDVIG_TYPE_FACE -> isEnabled(Feature.HEDVIG_TYPE_FACE, defaultValue = false)
        Feature.KEY_GEAR -> isEnabled(Feature.KEY_GEAR, defaultValue = false)
        Feature.EXTERNAL_DATA_COLLECTION -> isEnabled(Feature.EXTERNAL_DATA_COLLECTION, defaultValue = false)
    }

    private fun isEnabled(feature: Feature, defaultValue: Boolean): Boolean {
        return runBlocking {
            dataStore.data.map { it[booleanPreferencesKey(feature.name)] }.firstOrNull() ?: defaultValue
        }
    }

    override fun hasFeature(feature: Feature) = true
}
