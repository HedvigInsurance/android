package com.hedvig.app.feature.checkout

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.sequenceEither
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.EditMailAndSSNMutation
import com.hedvig.android.owldroid.graphql.QuoteCartEditQuoteMutation
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EditQuotesUseCase(
    private val apolloClient: ApolloClient,
    private val featureManager: FeatureManager,
    private val localeManager: LocaleManager,
) {

    data class Error(val message: String? = null)

    object Success

    suspend fun editAndSignQuotes(parameter: EditAndSignParameter): Either<Error, Success> = parameter.quoteIds
        .map { editQuote(it, parameter.quoteCartId, parameter.ssn, parameter.email) }
        .sequenceEither()
        .map { Success }

    private suspend fun editQuote(
        quoteId: String,
        quoteCartId: String?,
        ssn: String,
        email: String
    ): Either<Error, Success> {
        return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
            if (quoteCartId == null) {
                Either.Left(Error("No quote cart id found"))
            } else {
                editQuoteCart(quoteCartId, quoteId, ssn, email)
            }
        } else {
            editQuoteWithEmailAndSSN(quoteId, ssn, email)
        }
    }

    private suspend fun editQuoteCart(
        quoteCartId: String,
        quoteId: String,
        ssn: String,
        email: String
    ): Either<Error, Success> = apolloClient.mutate(createEditQuoteMutation(quoteCartId, ssn, email, quoteId))
        .safeQuery()
        .toEither()
        .mapLeft { Error(it.message) }
        .flatMap(::checkErrors)

    private fun checkErrors(data: QuoteCartEditQuoteMutation.Data) =
        data.quoteCart_editQuote.asQuoteBundleError?.let {
            Either.Left(Error(it.message))
        } ?: data.quoteCart_editQuote.asQuoteCart?.let {
            Either.Right(Success)
        } ?: Either.Left(Error())

    private fun createEditQuoteMutation(
        quoteCartId: String,
        ssn: String,
        email: String,
        quoteId: String
    ): QuoteCartEditQuoteMutation {

        val payload = Payload(
            ssn = ssn,
            email = email,
            data = Payload.Data(
                ssn = ssn,
                email = email,
            )
        )

        return QuoteCartEditQuoteMutation(
            quoteCartId,
            quoteId,
            Json.encodeToString(payload),
            localeManager.defaultLocale()
        )
    }

    private suspend fun editQuoteWithEmailAndSSN(
        quoteId: String,
        ssn: String,
        email: String
    ): Either<Error, Success> = apolloClient.mutate(EditMailAndSSNMutation(quoteId, ssn, email))
        .safeQuery()
        .toEither()
        .mapLeft { Error(it.message) }
        .flatMap(::checkErrors)

    private fun checkErrors(data: EditMailAndSSNMutation.Data): Either<Error, Success> =
        if (data.editQuote.asUnderwritingLimitsHit != null) {
            val codes = data.editQuote.asUnderwritingLimitsHit?.limits?.joinToString { it.code }
            Either.Left(Error(codes))
        } else {
            Either.Right(Success)
        }

    @Serializable
    data class Payload(
        val ssn: String,
        val email: String,
        val data: Data,
    ) {
        @Serializable
        data class Data(
            val ssn: String,
            val email: String,
        )
    }
}

data class EditAndSignParameter(
    val quoteIds: List<String>,
    val quoteCartId: String?,
    val ssn: String,
    val email: String
)
