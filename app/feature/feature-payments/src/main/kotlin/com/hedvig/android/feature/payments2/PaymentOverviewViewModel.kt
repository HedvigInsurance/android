package com.hedvig.android.feature.payments2

import com.hedvig.android.feature.payments2.data.AddDiscountUseCase
import com.hedvig.android.feature.payments2.data.GetUpcomingPaymentUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class PaymentOverviewViewModel(
  getUpcomingPaymentUseCase: GetUpcomingPaymentUseCase,
  addDiscountUseCase: AddDiscountUseCase,
) : MoleculeViewModel<PaymentEvent, OverViewUiState>(
    OverViewUiState(),
    PaymentOverviewPresenter(
      getUpcomingPaymentUseCase = getUpcomingPaymentUseCase,
      addDiscountUseCase = addDiscountUseCase,
    ),
  )
