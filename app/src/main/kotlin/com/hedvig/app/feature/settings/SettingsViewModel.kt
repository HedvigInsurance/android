package com.hedvig.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.UpdateLanguageMutation
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.market.Language
import com.hedvig.app.LanguageService
import com.hedvig.app.util.apollo.NetworkCacheManager
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch

class SettingsViewModel(
  hAnalytics: HAnalytics,
  private val changeDeviceLanguageAndUploadPreferredLanguageUseCase: ChangeDeviceLanguageAndUploadPreferredLanguageUseCase,
) : ViewModel() {
  init {
    hAnalytics.screenView(AppScreen.APP_SETTINGS)
  }

  fun applyLanguage(language: Language) {
    viewModelScope.launch {
      changeDeviceLanguageAndUploadPreferredLanguageUseCase.invoke(language)
    }
  }
}

class ChangeDeviceLanguageAndUploadPreferredLanguageUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
  private val cacheManager: NetworkCacheManager,
) {
  suspend fun invoke(language: Language) {
    apolloClient
      .mutation(
        UpdateLanguageMutation(language.toString(), language.toLocale()),
      ).safeExecute()
    cacheManager.clearCache()
    languageService.setLanguage(language)
  }
}
