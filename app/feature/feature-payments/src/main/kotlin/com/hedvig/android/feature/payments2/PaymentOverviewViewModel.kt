package com.hedvig.android.feature.payments2

import com.hedvig.android.feature.payments2.data.GetUpcomingPaymentUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class PaymentOverviewViewModel(
  getUpcomingPaymentUseCase: GetUpcomingPaymentUseCase,
) : MoleculeViewModel<PaymentEvent, OverViewUiState>(
    OverViewUiState.Loading,
    PaymentOverviewPresenter(
      getUpcomingPaymentUseCase = getUpcomingPaymentUseCase,
    ),
  )
