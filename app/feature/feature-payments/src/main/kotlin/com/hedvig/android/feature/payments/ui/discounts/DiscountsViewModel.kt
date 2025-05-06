package com.hedvig.android.feature.payments.ui.discounts

import com.hedvig.android.feature.payments.data.GetDiscountsOverviewUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class DiscountsViewModel(
  getDiscountsOverviewUseCase: GetDiscountsOverviewUseCase,
) : MoleculeViewModel<DiscountsEvent, DiscountsUiState>(
    DiscountsUiState(foreverInformation = null, discounts = listOf()),
    DiscountsPresenter(
      getDiscountsOverviewUseCase = getDiscountsOverviewUseCase,
    ),
  )
