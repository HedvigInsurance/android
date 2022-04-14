package com.hedvig.app.feature.home.ui.changeaddress

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ActiveContractBundlesQuery
import com.hedvig.app.feature.home.ui.changeaddress.GetAddressChangeStoryIdUseCase.SelfChangeEligibilityResult.Blocked
import com.hedvig.app.feature.home.ui.changeaddress.GetAddressChangeStoryIdUseCase.SelfChangeEligibilityResult.Eligible
import com.hedvig.app.feature.home.ui.changeaddress.GetAddressChangeStoryIdUseCase.SelfChangeEligibilityResult.Error
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager

class GetAddressChangeStoryIdUseCase(
    private val apolloClient: ApolloClient,
    private val featureManager: FeatureManager,
) {

    suspend operator fun invoke(): SelfChangeEligibilityResult {
        return when (val result = apolloClient.query(ActiveContractBundlesQuery()).safeQuery()) {
            is QueryResult.Success ->
                result.data
                    .activeContractBundles
                    .firstOrNull()
                    ?.addressChangeStoryId()
                    ?.let(::Eligible)
                    ?: Blocked
            is QueryResult.Error -> Error(result.message)
        }
    }

    sealed class SelfChangeEligibilityResult {
        data class Eligible(val embarkStoryId: String) : SelfChangeEligibilityResult()
        object Blocked : SelfChangeEligibilityResult()
        data class Error(val message: String?) : SelfChangeEligibilityResult()
    }

    private fun ActiveContractBundlesQuery.ActiveContractBundle.addressChangeStoryId(): String? {
        return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
            // angelStories.addressChangeV2
            angelStories.addressChange
        } else {
            angelStories.addressChange
        }
    }
}
