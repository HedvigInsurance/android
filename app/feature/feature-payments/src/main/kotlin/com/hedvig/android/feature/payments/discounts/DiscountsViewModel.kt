package com.hedvig.android.feature.payments.discounts

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.payments.overview.data.AddDiscountUseCase
import com.hedvig.android.feature.payments.overview.data.GetPaymentOverviewDataUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class DiscountsViewModel(
  getPaymentOverviewDataUseCase: Provider<GetPaymentOverviewDataUseCase>,
  addDiscountUseCase: AddDiscountUseCase,
) : MoleculeViewModel<DiscountsEvent, DiscountsUiState>(
    DiscountsUiState(foreverInformation = null),
    DiscountsPresenter(
      getPaymentOverviewDataUseCase = getPaymentOverviewDataUseCase,
      addDiscountUseCase = addDiscountUseCase,
    ),
  )
