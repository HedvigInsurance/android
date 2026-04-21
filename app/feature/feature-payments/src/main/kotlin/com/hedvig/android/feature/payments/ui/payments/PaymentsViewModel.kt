package com.hedvig.android.feature.payments.ui.payments

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.payments.overview.data.GetShouldShowPayoutUseCase
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class PaymentsViewModel(
  getUpcomingPaymentUseCase: Provider<GetUpcomingPaymentUseCase>,
  getShouldShowPayoutUseCase: Provider<GetShouldShowPayoutUseCase>,
) : MoleculeViewModel<PaymentsEvent, PaymentsUiState>(
    PaymentsUiState.Loading,
    PaymentsPresenter(
      getUpcomingPaymentUseCase = getUpcomingPaymentUseCase,
      getShouldShowPayoutUseCase = getShouldShowPayoutUseCase,
    ),
  )
