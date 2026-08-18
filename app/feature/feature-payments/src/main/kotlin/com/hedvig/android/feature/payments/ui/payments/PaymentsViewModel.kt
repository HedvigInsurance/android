package com.hedvig.android.feature.payments.ui.payments

import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.payments.overview.data.GetShouldShowPayoutUseCase
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class PaymentsViewModel(
  getUpcomingPaymentUseCase: GetUpcomingPaymentUseCase,
  getShouldShowPayoutUseCase: GetShouldShowPayoutUseCase,
) : MoleculeViewModel<PaymentsEvent, PaymentsUiState>(
    PaymentsUiState.Loading,
    PaymentsPresenter(
      getUpcomingPaymentUseCase = getUpcomingPaymentUseCase,
      getShouldShowPayoutUseCase = getShouldShowPayoutUseCase,
    ),
  )
