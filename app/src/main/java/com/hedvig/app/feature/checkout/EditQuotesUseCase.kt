package com.hedvig.app.feature.checkout

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.sequenceEither
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.EditMailAndSSNMutation
import com.hedvig.android.owldroid.graphql.QuoteCartEditQuoteMutation
import com.hedvig.app.feature.offer.model.QuoteCartId
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
) {

    data class Error(val message: String? = null)

    object Success

    suspend fun editQuotes(parameter: EditAndSignParameter): Either<Error, Success> = parameter.quoteIds
        .map { editQuote(it, parameter.quoteCartId, parameter.ssn, parameter.email) }
        .sequenceEither()
        .map { Success }

    private suspend fun editQuote(
        quoteId: String,
        quoteCartId: QuoteCartId?,
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
        quoteCartId: QuoteCartId,
        quoteId: String,
        ssn: String,
        email: String
    ): Either<Error, Success> {
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
            .mapLeft { Error() }
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
                    Error(errorCode).left()
                } else {
                    Success.right()
                }
            }
    }

    private suspend fun editQuoteWithEmailAndSSN(
        quoteId: String,
        ssn: String,
        email: String
    ): Either<Error, Success> = apolloClient.mutate(EditMailAndSSNMutation(quoteId, ssn, email))
        .safeQuery()
        .toEither { Error(it) }
        .flatMap(::checkErrors)

    private fun checkErrors(data: EditMailAndSSNMutation.Data): Either<Error, Success> =
        if (data.editQuote.asUnderwritingLimitsHit != null) {
            val codes = data.editQuote.asUnderwritingLimitsHit?.limits?.joinToString { it.code }
            Either.Left(Error(codes))
        } else {
            Either.Right(Success)
        }
}

data class EditAndSignParameter(
    val quoteIds: List<String>,
    val quoteCartId: QuoteCartId?,
    val ssn: String,
    val email: String
)
