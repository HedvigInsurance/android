package com.hedvig.android.feature.payments.overview

import com.hedvig.android.feature.payments.data.AddDiscountUseCase
import com.hedvig.android.feature.payments.data.GetUpcomingPaymentUseCase
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
