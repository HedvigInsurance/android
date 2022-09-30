package com.hedvig.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.market.Language
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch

class SettingsViewModel(
  hAnalytics: HAnalytics,
  private val changeLanguageUseCase: ChangeLanguageUseCase,
) : ViewModel() {
  init {
    hAnalytics.screenView(AppScreen.APP_SETTINGS)
  }

  fun applyLanguage(language: Language) {
    viewModelScope.launch {
      changeLanguageUseCase.invoke(language)
    }
  }
}
