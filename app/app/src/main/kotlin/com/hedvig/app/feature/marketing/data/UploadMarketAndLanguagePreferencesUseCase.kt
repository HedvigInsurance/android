package com.hedvig.app.feature.marketing.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import giraffe.UpdateLanguageMutation
import giraffe.type.Locale

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
        ifLeft = {
          logcat(LogPriority.ERROR) {
            "Failed to to upload language preferences to language:$languageTag | locale:$locale"
          }
        },
        ifRight = {
          logcat(LogPriority.INFO) {
            "Succeeded uploading language preferences to language:$languageTag | locale:$locale"
          }
        },
      )
  }
}
