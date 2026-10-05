package com.hedvig.android.feature.login.marketing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class MarketingPresenter(
  private val languageService: LanguageService,
) : MoleculePresenter<MarketingEvent, MarketingUiState> {
  @Composable
  override fun MoleculePresenterScope<MarketingEvent>.present(lastState: MarketingUiState): MarketingUiState {
    val language by languageService.language.collectAsState()

    CollectEvents { event ->
      when (event) {
        is MarketingEvent.SelectLanguage -> languageService.setLanguage(event.language)
      }
    }

    return MarketingUiState.Success(language)
  }
}

internal sealed interface MarketingUiState {
  data object Loading : MarketingUiState

  data class Success(
    val language: Language,
  ) : MarketingUiState
}

internal sealed interface MarketingEvent {
  data class SelectLanguage(val language: Language) : MarketingEvent
}
