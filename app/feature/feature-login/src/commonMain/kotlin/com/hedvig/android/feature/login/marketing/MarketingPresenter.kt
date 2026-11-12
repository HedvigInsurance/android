package com.hedvig.android.feature.login.marketing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hedvig.android.auth.BankIdSandboxState
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.launch

internal class MarketingPresenter(
  private val languageService: LanguageService,
  private val bankIdSandboxState: BankIdSandboxState,
) : MoleculePresenter<MarketingEvent, MarketingUiState> {
  @Composable
  override fun MoleculePresenterScope<MarketingEvent>.present(lastState: MarketingUiState): MarketingUiState {
    val language by languageService.language.collectAsState()
    val isBankIdSandboxEnabled by bankIdSandboxState.isEnabled.collectAsState()

    CollectEvents { event ->
      when (event) {
        is MarketingEvent.SelectLanguage -> languageService.setLanguage(event.language)
        is MarketingEvent.SetBankIdSandboxEnabled -> launch { bankIdSandboxState.setEnabled(event.enabled) }
      }
    }

    return MarketingUiState.Success(
      language = language,
      isBankIdSandboxAvailable = bankIdSandboxState.isAvailable,
      isBankIdSandboxEnabled = isBankIdSandboxEnabled,
    )
  }
}

internal sealed interface MarketingUiState {
  data object Loading : MarketingUiState

  data class Success(
    val language: Language,
    val isBankIdSandboxAvailable: Boolean,
    val isBankIdSandboxEnabled: Boolean,
  ) : MarketingUiState
}

internal sealed interface MarketingEvent {
  data class SelectLanguage(val language: Language) : MarketingEvent

  data class SetBankIdSandboxEnabled(val enabled: Boolean) : MarketingEvent
}
