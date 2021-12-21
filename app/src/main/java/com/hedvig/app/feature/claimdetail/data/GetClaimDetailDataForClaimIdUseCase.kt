package com.hedvig.app.feature.claimdetail.data

import arrow.core.Option
import arrow.core.firstOrNone
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ClaimDetailsQuery
import com.hedvig.app.feature.claimdetail.model.ClaimDetailsData
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery

class GetClaimDetailDataForClaimIdUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    suspend operator fun invoke(claimId: String): Option<ClaimDetailsData> {
        return apolloClient
            .query(ClaimDetailsQuery(localeManager.defaultLocale()))
            .safeQuery()
            .toOption()
            .flatMap { data ->
                data.claimDetails.firstOrNone { it.id == claimId }
            }
            .map(ClaimDetailsData::fromDto)
    }
}
