package com.hedvig.app.feature.checkout

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.right
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.apollo.GraphQLQueryHandler
import giraffe.QuoteCartEditQuoteMutation
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.json.JSONException
import org.json.JSONObject

class EditCheckoutUseCase(
  private val languageService: LanguageService,
  private val graphQLQueryHandler: GraphQLQueryHandler,
) {

  object Success

  suspend fun editQuotes(parameter: EditAndSignParameter): Either<ErrorMessage, Success> {
    return editQuoteCart(parameter)
  }

  private suspend fun editQuoteCart(
    parameter: EditAndSignParameter,
  ): Either<ErrorMessage, Success> = either {
    ensureNotNull(parameter.quoteCartId) { ErrorMessage("No quote cart id found") }
    parameter
      .quoteIds
      .map { mutateQuoteCart(parameter.quoteCartId, it, parameter.ssn, parameter.email) }
      .bindAll()
      .first()
  }

  private suspend fun mutateQuoteCart(
    quoteCartId: QuoteCartId,
    quoteId: String,
    ssn: String,
    email: String,
  ): Either<ErrorMessage, Success> {
    val json = buildJsonObject {
      put("quoteCartId", quoteCartId.id)
      put("quoteId", quoteId)
      put("locale", languageService.getGraphQLLocale().rawValue)
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
      query = QuoteCartEditQuoteMutation.OPERATION_DOCUMENT,
      variables = JSONObject(json),
      files = emptyList(),
    )
      .toEither(::ErrorMessage)
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

data class EditAndSignParameter(
  val quoteIds: List<String>,
  val quoteCartId: QuoteCartId?,
  val ssn: String,
  val email: String,
)
