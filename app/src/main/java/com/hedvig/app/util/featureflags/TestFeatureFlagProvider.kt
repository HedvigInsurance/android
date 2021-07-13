package com.hedvig.app.util.featureflags

object TestFeatureFlagProvider : FeatureFlagProvider {

    private val features = HashMap<Feature, Boolean>()

    override val priority = TEST_PRIORITY

    override fun isFeatureEnabled(feature: Feature): Boolean = features[feature]!!

    override fun hasFeature(feature: Feature): Boolean = features.containsKey(feature)

    fun setFeatureEnabled(feature: Feature, enabled: Boolean) = features.put(feature, enabled)

    fun clearFeatures() = features.clear()
}
