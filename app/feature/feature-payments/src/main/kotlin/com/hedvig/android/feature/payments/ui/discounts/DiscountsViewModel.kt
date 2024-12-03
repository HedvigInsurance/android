package com.hedvig.android.feature.payments.ui.discounts

import com.hedvig.android.feature.payments.data.GetDiscountsOverviewUseCase
import com.hedvig.android.feature.payments.overview.data.AddDiscountUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class DiscountsViewModel(
  getDiscountsOverviewUseCase: GetDiscountsOverviewUseCase,
  addDiscountUseCase: AddDiscountUseCase,
) : MoleculeViewModel<DiscountsEvent, DiscountsUiState>(
    DiscountsUiState(foreverInformation = null, discounts = listOf()),
    DiscountsPresenter(
      getDiscountsOverviewUseCase = getDiscountsOverviewUseCase,
      addDiscountUseCase = addDiscountUseCase,
    ),
  )
