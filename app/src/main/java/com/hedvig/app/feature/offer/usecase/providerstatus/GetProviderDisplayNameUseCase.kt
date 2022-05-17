package com.hedvig.app.feature.offer.usecase.providerstatus

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.http.HttpFetchPolicy
import com.apollographql.apollo3.cache.http.httpFetchPolicy
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.owldroid.graphql.ProviderStatusQuery
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetProviderDisplayNameUseCase(
    private val apolloClient: ApolloClient,
) {
    /**
     * [insuranceCompany] is the code name for companies that usually start with a "se" prefix and have dashes instead
     * of spaces.
     * An example input is "se-demo"
     */
    suspend operator fun invoke(insuranceCompany: String?): String? {
        if (insuranceCompany == null) return null
        val result = apolloClient
            .query(ProviderStatusQuery())
            .httpFetchPolicy(HttpFetchPolicy.CacheFirst) // Names aren't going to change often if ever, prefer cache
            .fetchPolicy(FetchPolicy.CacheFirst)
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
