package com.hedvig.app.feature.offer.ui.changestartdate

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.computations.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.QuoteCartEditQuoteMutation
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class QuoteCartEditStartDateUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {

    object Success

    suspend fun removeStartDate(
        quoteCartId: CreateQuoteCartUseCase.QuoteCartId,
        quoteId: String,
    ): Either<ErrorMessage, Success> {
        return mutateQuoteCartAndUpdate(
            quoteCartId,
            quoteId,
            null
        )
    }

    suspend fun setStartDate(
        quoteCartId: CreateQuoteCartUseCase.QuoteCartId,
        quoteId: String,
        date: LocalDate,
    ): Either<ErrorMessage, Success> {
        return mutateQuoteCartAndUpdate(
            quoteCartId,
            quoteId,
            date
        )
    }

    private suspend fun mutateQuoteCartAndUpdate(
        quoteCartId: CreateQuoteCartUseCase.QuoteCartId,
        quoteId: String,
        startDate: LocalDate?,
    ): Either<ErrorMessage, Success> {
        val json = createPayload(startDate)
        val mutation = QuoteCartEditQuoteMutation(quoteCartId.id, quoteId, json, localeManager.defaultLocale())

        return either {
            val result = apolloClient.mutate(mutation)
                .safeQuery()
                .toEither(::ErrorMessage)
                .bind()

            ensureNotNull(result.quoteCart_editQuote.asQuoteCart) {
                result.quoteCart_editQuote.asQuoteBundleError?.let { asQuoteBundleError ->
                    asQuoteBundleError.limits?.let {
                        ErrorMessage(it.joinToString())
                    } ?: ErrorMessage(asQuoteBundleError.message)
                } ?: ErrorMessage()
            }

            Success
        }
    }

    private fun createPayload(startDate: LocalDate?): String {
        val formattedStartDate = startDate?.format(DateTimeFormatter.ISO_DATE)
        return buildJsonObject {
            put("startDate", formattedStartDate)
            putJsonObject("data") {
                put("startDate", formattedStartDate)
            }
        }.toString()
    }
}
