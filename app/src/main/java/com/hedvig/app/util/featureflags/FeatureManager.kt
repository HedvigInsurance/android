package com.hedvig.app.util.featureflags

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.isDebug
import com.hedvig.app.service.RemoteConfig
import com.hedvig.hanalytics.HAnalytics
import java.util.concurrent.CopyOnWriteArrayList

data class FeatureManager(
    private val marketManager: MarketManager,
    private val remoteConfig: RemoteConfig,
    private val dataStore: DataStore<Preferences>,
    private val hAnalytics: HAnalytics,
) {

    @VisibleForTesting
    internal val providers = CopyOnWriteArrayList<FeatureFlagProvider>()

    init {
        if (isDebug()) {
            addProvider(DebugFeatureFlagProvider(dataStore))
        } else {
            addProvider(RemoteFeatureFlagProvider(marketManager, remoteConfig))
            addProvider(ProductionFeatureFlagProvider(marketManager))
        }

        addProvider(HAnalyticsFeatureFlagProvider(hAnalytics))
    }

    suspend fun isFeatureEnabled(feature: Feature): Boolean {
        return providers
            .filter { it.hasFeature(feature) }
            .minByOrNull(FeatureFlagProvider::priority)
            ?.isFeatureEnabled(feature)
            ?: feature.enabledByDefault
    }

    private fun addProvider(provider: FeatureFlagProvider) = providers.add(provider)
}
