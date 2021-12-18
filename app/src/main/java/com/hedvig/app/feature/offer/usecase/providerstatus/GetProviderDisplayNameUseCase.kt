package com.hedvig.app.feature.offer.usecase.providerstatus

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.hedvig.android.owldroid.graphql.ProviderStatusQuery
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetProviderDisplayNameUseCase(
    private val apolloClient: ApolloClient,
) {
    /**
     * [insuranceCompany] is the code name for companies that usually start with a "se" prefix and have dashes instead
     * of spaces, like "se-demo"
     */
    suspend operator fun invoke(insuranceCompany: String?): String? {
        if (insuranceCompany == null) return null
        val result = apolloClient
            .query(ProviderStatusQuery())
            .toBuilder()
            .httpCachePolicy(HttpCachePolicy.CACHE_FIRST) // Names aren't going to change often if ever, prefer cache
            .build()
            .safeQuery()
        if (result is QueryResult.Success) {
            return result.data.externalInsuranceProvider
                ?.providerStatusV2
                ?.firstOrNull { it.insuranceProvider == insuranceCompany }
                ?.insuranceProviderDisplayName
        }
        return null
    }
}
