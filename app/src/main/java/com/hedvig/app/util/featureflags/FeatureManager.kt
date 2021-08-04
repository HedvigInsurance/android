package com.hedvig.app.util.featureflags

import androidx.annotation.VisibleForTesting
import com.hedvig.app.feature.settings.MarketManager
import java.util.concurrent.CopyOnWriteArrayList

class FeatureManager(
    val marketManager: MarketManager,
    isDebugBuild: Boolean
) {

    @VisibleForTesting
    internal val providers = CopyOnWriteArrayList<FeatureFlagProvider>()

    init {
        if (isDebugBuild) {
            addProvider(TestFeatureFlagProvider)
            addProvider(DebugFeatureFlagProvider(marketManager))
        } else {
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
