package com.hedvig.android.feature.payments.ui.payments

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class PaymentsViewModel(
  getUpcomingPaymentUseCase: Provider<GetUpcomingPaymentUseCase>,
) : MoleculeViewModel<PaymentsEvent, PaymentsUiState>(
    PaymentsUiState.Loading,
    PaymentsPresenter(
      getUpcomingPaymentUseCase = getUpcomingPaymentUseCase,
    ),
  )
