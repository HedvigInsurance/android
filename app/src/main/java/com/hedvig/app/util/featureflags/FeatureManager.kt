package com.hedvig.app.util.featureflags

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.tracking.HAnalyticsFacade
import com.hedvig.app.isDebug
import com.hedvig.app.service.RemoteConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

data class FeatureManager(
    private val marketManager: MarketManager,
    private val remoteConfig: RemoteConfig,
    private val dataStore: DataStore<Preferences>,
    private val hAnalyticsFacade: HAnalyticsFacade,
) {

    @VisibleForTesting
    internal val providers = CopyOnWriteArrayList<FeatureFlagProvider>()

    init {
        GlobalScope.launch {
            hAnalyticsFacade.loadExperimentsFromServer()

            if (isDebug()) {
                addProvider(DebugFeatureFlagProvider(dataStore))
            } else {
                addProvider(RemoteFeatureFlagProvider(marketManager, remoteConfig))
                addProvider(ProductionFeatureFlagProvider(marketManager))
            }

            addProvider(HAnalyticsFeatureFlagProvider(hAnalyticsFacade))
        }
    }

    fun isFeatureEnabled(feature: Feature): Boolean {
        return providers.filter { it.hasFeature(feature) }
            .minByOrNull(FeatureFlagProvider::priority)
            ?.isFeatureEnabled(feature)
            ?: feature.enabledByDefault
    }

    private fun addProvider(provider: FeatureFlagProvider) = providers.add(provider)
}
