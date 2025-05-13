package com.hedvig.android.feature.login.marketing

import com.hedvig.android.language.LanguageService
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class MarketingViewModel(
  languageService: LanguageService,
) : MoleculeViewModel<MarketingEvent, MarketingUiState>(
    MarketingUiState.Loading,
    MarketingPresenter(languageService),
  )
