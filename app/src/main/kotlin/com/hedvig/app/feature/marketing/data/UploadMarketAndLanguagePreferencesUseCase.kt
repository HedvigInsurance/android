package com.hedvig.app.feature.marketing.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.UpdateLanguageMutation
import com.hedvig.android.apollo.graphql.type.Locale
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService
import slimber.log.e
import slimber.log.i

class UploadMarketAndLanguagePreferencesUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun invoke() {
    val languageTag: String = languageService.getLocale().toLanguageTag()
    val locale: Locale = languageService.getGraphQLLocale()
    apolloClient
      .mutation(UpdateLanguageMutation(languageTag, locale))
      .safeExecute()
      .toEither()
      .fold(
        ifLeft = { e { "Failed to to upload language preferences to language:$languageTag | locale:$locale" } },
        ifRight = { i { "Succeeded uploading language preferences to language:$languageTag | locale:$locale" } },
      )
  }
}
