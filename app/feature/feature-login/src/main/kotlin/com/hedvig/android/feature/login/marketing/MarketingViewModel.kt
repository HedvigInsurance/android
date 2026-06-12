package com.hedvig.android.feature.login.marketing

import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.language.LanguageService
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel
internal class MarketingViewModel(
  languageService: LanguageService,
) : MoleculeViewModel<MarketingEvent, MarketingUiState>(
    MarketingUiState.Loading,
    MarketingPresenter(languageService),
  )
