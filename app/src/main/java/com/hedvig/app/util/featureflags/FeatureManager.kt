package com.hedvig.app.util.featureflags

import androidx.annotation.VisibleForTesting
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.isDebug
import java.util.concurrent.CopyOnWriteArrayList

object FeatureManager {

    @VisibleForTesting
    internal val providers = CopyOnWriteArrayList<FeatureFlagProvider>()

    init {
        if (isDebug()) {
            addProvider(DebugFeatureFlagProvider())
        } else {
            addProvider(ProductionFeatureFlagProvider())
        }
    }

    fun isFeatureEnabled(feature: Feature, market: Market?): Boolean {
        return providers.filter { it.hasFeature(feature) }
            .minByOrNull(FeatureFlagProvider::priority)
            ?.isFeatureEnabled(feature, market)
            ?: feature.enabledByDefault
    }

    private fun addProvider(provider: FeatureFlagProvider) = providers.add(provider)
}
