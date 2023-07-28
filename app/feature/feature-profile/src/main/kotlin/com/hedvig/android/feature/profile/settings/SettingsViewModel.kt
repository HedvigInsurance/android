package com.hedvig.android.feature.profile.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.datastore.SettingsDataStore
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.theme.Theme
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
  hAnalytics: HAnalytics,
  private val changeLanguageUseCase: ChangeLanguageUseCase,
  marketManager: MarketManager,
  private val languageService: LanguageService,
  private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

  private val _uiState = MutableStateFlow(
    SettingsUiState(
      selectedLanguage = languageService.getLanguage(),
      languageOptions = getLanguageOptionsFromMarket(marketManager.market),
      selectedTheme = Theme.LIGHT,
      themeOptions = listOf(Theme.LIGHT),
      notificationOn = true,
      showNotificationInfo = true,
    ),
  )
  val uiState: StateFlow<SettingsUiState> = _uiState

  init {
    hAnalytics.screenView(AppScreen.APP_SETTINGS)

    settingsDataStore
      .observeTheme()
      .onEach { theme ->
        _uiState.update {
          it.copy(selectedTheme = theme)
        }
      }
      .launchIn(viewModelScope)
  }

  private fun getLanguageOptionsFromMarket(market: Market?): List<Language> = when (market) {
    Market.SE -> listOf(Language.EN_SE, Language.SV_SE)
    Market.NO -> listOf(Language.EN_NO, Language.NB_NO)
    Market.DK -> listOf(Language.EN_DK, Language.DA_DK)
    Market.FR -> listOf(Language.EN_FR, Language.FR_FR)
    null -> listOf(Language.EN_SE, Language.SV_SE)
  }

  fun applyLanguage(language: Language) {
    viewModelScope.launch {
      changeLanguageUseCase.invoke(language)
      languageService.setLanguage(language)
      _uiState.update { it.copy(selectedLanguage = language) }
    }
  }

  fun applyTheme(theme: Theme) {
    viewModelScope.launch {
      settingsDataStore.setTheme(theme)
    }
  }

  fun dismissNotificationInfo() {
    _uiState.update {
      it.copy(showNotificationInfo = false)
    }
  }

  data class SettingsUiState(
    val selectedLanguage: Language,
    val languageOptions: List<Language>,
    val selectedTheme: Theme,
    val themeOptions: List<Theme>,
    val notificationOn: Boolean,
    val showNotificationInfo: Boolean,
  )
}
