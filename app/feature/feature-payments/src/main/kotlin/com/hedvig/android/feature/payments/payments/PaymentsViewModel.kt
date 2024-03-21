package com.hedvig.android.feature.payments.payments

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.payments.overview.data.GetPaymentOverviewDataUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed

internal class PaymentsViewModel(
  getPaymentOverviewDataUseCase: Provider<GetPaymentOverviewDataUseCase>,
) : MoleculeViewModel<PaymentsEvent, PaymentsUiState>(
    PaymentsUiState.Loading,
    PaymentsPresenter(
      getPaymentOverviewDataUseCase = getPaymentOverviewDataUseCase,
    ),
    SharingStarted.WhileSubscribed(0.seconds),
  )
