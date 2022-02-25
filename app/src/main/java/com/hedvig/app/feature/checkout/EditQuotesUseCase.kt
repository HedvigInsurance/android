package com.hedvig.app.feature.checkout

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.sequenceEither
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.EditMailAndSSNMutation
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.CacheManager
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.FeatureManager

class EditQuotesUseCase(
    private val apolloClient: ApolloClient,
    private val cacheManager: CacheManager,
    private val featureManager: FeatureManager,
    private val localeManager: LocaleManager,
) {

    data class Error(val message: String? = null)

    object Success

    suspend fun editAndSignQuotes(parameter: EditAndSignParameter): Either<Error, Success> = parameter.quoteIds
        .map { editQuote(it, parameter.ssn, parameter.email) }
        .sequenceEither()
        .map { Success }

    private suspend fun editQuote(
        quoteId: String,
        ssn: String,
        email: String
    ): Either<Error, Success> = apolloClient.mutate(EditMailAndSSNMutation(quoteId, ssn, email))
        .safeQuery()
        .toEither()
        .mapLeft { Error(it.message) }
        .flatMap { checkErrors(it) }

    private fun checkErrors(it: EditMailAndSSNMutation.Data): Either<Error, Success> =
        if (it.editQuote.asUnderwritingLimitsHit != null) {
            val codes = it.editQuote.asUnderwritingLimitsHit?.limits?.joinToString { it.code }
            Either.Left(Error(codes))
        } else {
            Either.Right(Success)
        }
}

data class EditAndSignParameter(
    val quoteIds: List<String>,
    val quoteCartId: String?,
    val ssn: String,
    val email: String
)
