package com.hedvig.app.util.featureflags

interface FeatureFlagProvider {
    val priority: Int
    suspend fun isFeatureEnabled(feature: Feature): Boolean
    fun hasFeature(feature: Feature): Boolean
}

const val DEBUG_PRIORITY = 1
const val REMOTE_PRIORITY = 2
const val PRODUCTION_PRIORITY = 3
const val HANALYTICS_PRIORITY = 4
