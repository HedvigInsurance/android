package com.hedvig.app.feature.insurance.data

import arrow.core.Either
import arrow.core.computations.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery

class GetContractsUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    suspend fun invoke(): Either<ErrorMessage, InsuranceQuery.Data> {
        return either {
            val insuranceQueryData = apolloClient
                .query(InsuranceQuery(localeManager.defaultLocale()))
                .safeQuery()
                .toEither(::ErrorMessage)
                .bind()
            insuranceQueryData
        }
    }
}
