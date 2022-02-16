package com.hedvig.app.feature.onboarding

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.android.owldroid.type.EmbarkStoryType
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetBundlesUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) {
    suspend operator fun invoke(): BundlesResult {
        val locale = localeManager.defaultLocale().rawValue
        val choosePlanQuery = ChoosePlanQuery(locale)
        return when (val result = apolloClient.query(choosePlanQuery).safeQuery()) {
            is QueryResult.Error -> BundlesResult.Error
            is QueryResult.Success -> result.data.mapToSuccess()
        }
    }
}

fun ChoosePlanQuery.Data.mapToSuccess(): BundlesResult.Success {
    val appStories = embarkStories.filter { it.type == EmbarkStoryType.APP_ONBOARDING }
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
