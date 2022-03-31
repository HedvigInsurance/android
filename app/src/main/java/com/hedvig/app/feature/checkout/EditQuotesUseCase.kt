package com.hedvig.app.feature.checkout

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.computations.ensureNotNull
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.sequenceEither
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.EditMailAndSSNMutation
import com.hedvig.android.owldroid.graphql.QuoteCartEditQuoteMutation
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.GraphQLQueryHandler
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.json.JSONException
import org.json.JSONObject

class EditQuotesUseCase(
    private val apolloClient: ApolloClient,
    private val featureManager: FeatureManager,
    private val localeManager: LocaleManager,
    private val graphQLQueryHandler: GraphQLQueryHandler,
    private val offerRepository: OfferRepository,
) {

    object Success

    suspend fun editQuotes(parameter: EditAndSignParameter): Either<ErrorMessage, Success> {
        return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
            editQuoteCart(parameter)
        } else {
            parameter.quoteIds
                .map { mutateQuoteWithEmailAndSSN(it, parameter.ssn, parameter.email) }
                .sequenceEither()
                .map { Success }
        }
    }

    private suspend fun editQuoteCart(
        parameter: EditAndSignParameter
    ): Either<ErrorMessage, Success> = either {
        ensureNotNull(parameter.quoteCartId) { ErrorMessage("No quote cart id found") }
        val quoteIds = offerRepository.getQuoteIds(parameter.quoteCartId).bind()
        val results = quoteIds
            .map { mutateQuoteCart(parameter.quoteCartId, it, parameter.ssn, parameter.email) }
            .sequenceEither().bind()
        results.first()
    }

    private suspend fun mutateQuoteCart(
        quoteCartId: QuoteCartId,
        quoteId: String,
        ssn: String,
        email: String
    ): Either<ErrorMessage, Success> {
        val json = buildJsonObject {
            put("quoteCartId", quoteCartId.id)
            put("quoteId", quoteId)
            put("locale", localeManager.defaultLocale().rawValue)
            putJsonObject("payload") {
                put("ssn", ssn)
                put("email", email)
                putJsonObject("data") {
                    put("ssn", ssn)
                    put("email", email)
                }
            }
        }.toString()

        return graphQLQueryHandler.graphQLQuery(
            query = QuoteCartEditQuoteMutation.QUERY_DOCUMENT,
            variables = JSONObject(json),
            files = emptyList()
        )
            .toEither()
            .mapLeft { ErrorMessage(it.message) }
            .flatMap { jsonResponse ->
                val errorCode = try {
                    jsonResponse.getJSONObject("data")
                        .getJSONObject("quoteCart_editQuote")
                        .getJSONArray("limits")
                        .getJSONObject(0)
                        .getString("code")
                } catch (exception: JSONException) {
                    null
                }

                if (errorCode != null) {
                    ErrorMessage(errorCode).left()
                } else {
                    Success.right()
                }
            }
    }

    private suspend fun mutateQuoteWithEmailAndSSN(
        quoteId: String,
        ssn: String,
        email: String
    ): Either<ErrorMessage, Success> = apolloClient.mutate(EditMailAndSSNMutation(quoteId, ssn, email))
        .safeQuery()
        .toEither { ErrorMessage(it) }
        .flatMap(::checkErrors)

    private fun checkErrors(data: EditMailAndSSNMutation.Data): Either<ErrorMessage, Success> =
        if (data.editQuote.asUnderwritingLimitsHit != null) {
            val codes = data.editQuote.asUnderwritingLimitsHit?.limits?.joinToString { it.code }
            ErrorMessage(codes).left()
        } else {
            Success.right()
        }
}

data class EditAndSignParameter(
    val quoteIds: List<String>,
    val quoteCartId: QuoteCartId?,
    val ssn: String,
    val email: String,
)
