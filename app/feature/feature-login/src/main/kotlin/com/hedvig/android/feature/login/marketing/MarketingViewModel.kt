package com.hedvig.android.feature.login.marketing

import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.android.market.set.SetMarketUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class MarketingViewModel(
  marketManager: MarketManager,
  languageService: LanguageService,
  setMarketUseCase: SetMarketUseCase,
) : MoleculeViewModel<MarketingEvent, MarketingUiState>(
    MarketingUiState.Loading,
    MarketingPresenter(marketManager, languageService, setMarketUseCase),
  )
