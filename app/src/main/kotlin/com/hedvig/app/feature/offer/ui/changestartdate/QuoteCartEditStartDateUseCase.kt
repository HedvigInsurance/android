package com.hedvig.app.feature.offer.ui.changestartdate

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.QuoteCartEditQuoteMutation
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class QuoteCartEditStartDateUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {

  object Success

  suspend fun removeStartDate(
    quoteCartId: QuoteCartId,
    quoteId: String,
  ): Either<ErrorMessage, Success> {
    return mutateQuoteCartAndUpdate(
      quoteCartId,
      quoteId,
      null,
    )
  }

  suspend fun setStartDate(
    quoteCartId: QuoteCartId,
    quoteId: String,
    date: LocalDate,
  ): Either<ErrorMessage, Success> {
    return mutateQuoteCartAndUpdate(
      quoteCartId,
      quoteId,
      date,
    )
  }

  private suspend fun mutateQuoteCartAndUpdate(
    quoteCartId: QuoteCartId,
    quoteId: String,
    startDate: LocalDate?,
  ): Either<ErrorMessage, Success> {
    val json = createPayload(startDate)
    val mutation = QuoteCartEditQuoteMutation(quoteCartId.id, quoteId, json, languageService.getGraphQLLocale())

    return either {
      val result = apolloClient.mutation(mutation)
        .safeExecute()
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
