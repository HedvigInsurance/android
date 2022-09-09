package com.hedvig.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.apollo.graphql.type.Locale
import com.hedvig.app.feature.marketpicker.LanguageRepository
import com.hedvig.app.feature.marketpicker.LocaleBroadcastManager
import com.hedvig.app.util.apollo.NetworkCacheManager
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val repository: LanguageRepository,
  private val localeBroadcastManager: LocaleBroadcastManager,
  hAnalytics: HAnalytics,
  private val cacheManager: NetworkCacheManager,
) : ViewModel() {
  init {
    hAnalytics.screenView(AppScreen.APP_SETTINGS)
  }

  fun save(acceptLanguage: String, locale: Locale) {
    viewModelScope.launch {
      repository.uploadLanguage(acceptLanguage, locale)
    }
    cacheManager.clearCache()
    localeBroadcastManager.sendBroadcast(recreate = true)
  }
}
