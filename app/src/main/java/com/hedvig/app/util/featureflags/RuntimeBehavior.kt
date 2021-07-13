package com.hedvig.app.util.featureflags

import androidx.annotation.VisibleForTesting
import com.hedvig.app.feature.settings.MarketManager
import java.util.concurrent.CopyOnWriteArrayList

object RuntimeBehavior {

    @JvmStatic
    fun initialize(
        marketManager: MarketManager,
        isDebugBuild: Boolean
    ) {
        if (isDebugBuild) {
            addProvider(TestFeatureFlagProvider)
            addProvider(DebugFeatureFlagProvider(marketManager))
        } else {
            addProvider(ProductionFeatureFlagProvider(marketManager))
        }
    }

    @VisibleForTesting
    internal val providers = CopyOnWriteArrayList<FeatureFlagProvider>()

    @JvmStatic
    fun isFeatureEnabled(feature: Feature): Boolean {
        return providers.filter { it.hasFeature(feature) }
            .sortedBy(FeatureFlagProvider::priority)
            .firstOrNull()
            ?.isFeatureEnabled(feature)
            ?: feature.enabledByDefault
    }

    @JvmStatic
    fun addProvider(provider: FeatureFlagProvider) = providers.add(provider)
}
