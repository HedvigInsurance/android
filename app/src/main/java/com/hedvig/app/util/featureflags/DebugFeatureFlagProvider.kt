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

    override fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> isEnabled(Feature.MOVING_FLOW)
        Feature.FRANCE_MARKET -> isEnabled(Feature.FRANCE_MARKET)
        Feature.ADDRESS_AUTO_COMPLETE -> isEnabled(Feature.ADDRESS_AUTO_COMPLETE)
        Feature.REFERRAL_CAMPAIGN -> isEnabled(Feature.REFERRAL_CAMPAIGN)
        Feature.QUOTE_CART -> false
    }

    private fun isEnabled(feature: Feature): Boolean {
        return runBlocking {
            dataStore.data.map { it[booleanPreferencesKey(feature.name)] }.firstOrNull() ?: true
        }
    }

    override fun hasFeature(feature: Feature) = true
}
