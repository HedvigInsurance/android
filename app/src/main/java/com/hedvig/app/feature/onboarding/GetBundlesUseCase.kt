package com.hedvig.app.feature.onboarding

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.android.owldroid.type.EmbarkStoryType
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature

class GetBundlesUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
    private val featureManager: FeatureManager,
) {
    private suspend fun storyType() = if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
        EmbarkStoryType.APP_ONBOARDING_QUOTE_CART
    } else {
        EmbarkStoryType.APP_ONBOARDING
    }

    suspend operator fun invoke(): BundlesResult {
        val locale = localeManager.defaultLocale().rawValue
        val choosePlanQuery = ChoosePlanQuery(locale)
        return when (val result = apolloClient.query(choosePlanQuery).safeQuery()) {
            is QueryResult.Error -> BundlesResult.Error
            is QueryResult.Success -> result.data.mapToSuccess(storyType())
        }
    }
}

fun ChoosePlanQuery.Data.mapToSuccess(storyType: EmbarkStoryType): BundlesResult.Success {
    val appStories = embarkStories.filter { it.type == storyType }
    val bundles = appStories.map {
        BundlesResult.Success.Bundle(
            storyName = it.name,
            storyTitle = it.title,
            description = it.description,
            discountText = it.metadata.find { it.asEmbarkStoryMetadataEntryDiscount != null }
                ?.asEmbarkStoryMetadataEntryDiscount
                ?.discount
        )
    }
    return BundlesResult.Success(bundles)
}

sealed class BundlesResult {
    data class Success(
        val bundles: List<Bundle>
    ) : BundlesResult() {
        data class Bundle(
            val storyName: String,
            val storyTitle: String,
            val description: String,
            val discountText: String?,
        )
    }

    object Error : BundlesResult()
}
