package com.hedvig.app.feature.offer.ui.changestartdate

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.QuoteCartEditQuoteMutation
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.GraphQLQueryHandler
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class QuoteCartEditStartDateUseCase(
    private val apolloClient: ApolloClient,
    private val offerRepository: OfferRepository,
    private val localeManager: LocaleManager,
    private val graphQLQueryHandler: GraphQLQueryHandler,
) {

    object Success

    suspend fun removeStartDate(
        quoteCartId: CreateQuoteCartUseCase.QuoteCartId,
        quoteId: String,
        idsInBundle: List<String>
    ) {
    }

    suspend fun setStartDate(
        quoteCartId: CreateQuoteCartUseCase.QuoteCartId,
        quoteId: String,
        idsInBundle: List<String>,
        date: LocalDate
    ): Either<ErrorMessage, Success> {
        return mutateQuoteCart(
            quoteCartId,
            quoteId,
            date
        )
    }

    private suspend fun mutateQuoteCart(
        quoteCartId: CreateQuoteCartUseCase.QuoteCartId,
        quoteId: String,
        startDate: LocalDate,
    ): Either<ErrorMessage, Success> {
        val formattedStartDate = startDate.format(DateTimeFormatter.ISO_DATE)
        val json = buildJsonObject {
            put("quoteCartId", quoteCartId.id)
            put("quoteId", quoteId)
            put("locale", localeManager.defaultLocale().rawValue)
            putJsonObject("payload") {
                put("startDate", formattedStartDate)
                putJsonObject("data") {
                    put("startDate", formattedStartDate)
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
}
