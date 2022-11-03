package com.hedvig.app.feature.marketing.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.UpdateLanguageMutation
import com.hedvig.android.apollo.graphql.type.Locale
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import e
import i

class UploadMarketAndLanguagePreferencesUseCase(
  private val apolloClient: ApolloClient,
  private val marketManager: MarketManager,
  private val languageService: LanguageService,
) {
  suspend fun invoke() {
    val languageTag: String = languageService.getLocale().toLanguageTag()
    val locale: Locale = languageService.getGraphQLLocale()
    apolloClient
      .mutation(UpdateLanguageMutation(languageTag, locale))
      .safeExecute()
      .toEither()
      .tap { marketManager.hasSelectedMarket = true }
      .fold(
        { i { "Succeeded uploading language preferences to language:$languageTag | locale:$locale" } },
        { e { "Failed to to upload language preferences to language:$languageTag | locale:$locale" } },
      )
  }
}
