package com.hedvig.app.feature.marketpicker

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.UpdateLanguageMutation
import com.hedvig.android.apollo.graphql.type.Locale
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import e
import i

class LanguageRepository(
  private val apolloClient: ApolloClient,
) {

  suspend fun uploadLanguage(acceptLanguage: String, locale: Locale) {
    val response = apolloClient
      .mutation(UpdateLanguageMutation(acceptLanguage, locale))
      .safeExecute()
      .toEither()
    when (response) {
      is Either.Left -> e { "Failed to update language: Error message: ${response.value.message}" }
      is Either.Right -> i { "Successfully updated language" }
    }
  }
}
