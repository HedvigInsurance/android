package com.hedvig.app.util.featureflags

import androidx.annotation.VisibleForTesting
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.isDebug
import com.hedvig.app.service.RemoteConfig
import java.util.concurrent.CopyOnWriteArrayList

data class FeatureManager(
    private val marketManager: MarketManager,
    private val remoteConfig: RemoteConfig,
) {

    @VisibleForTesting
    internal val providers = CopyOnWriteArrayList<FeatureFlagProvider>()

    init {
        if (isDebug()) {
            addProvider(DebugFeatureFlagProvider(marketManager))
        } else {
            addProvider(RemoteFeatureFlagProvider(marketManager, remoteConfig))
            addProvider(ProductionFeatureFlagProvider(marketManager))
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
