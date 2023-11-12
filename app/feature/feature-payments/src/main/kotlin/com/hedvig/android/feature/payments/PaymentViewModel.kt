package com.hedvig.android.feature.payments

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.payments.data.PaymentRepository
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class PaymentViewModel(
  private val paymentRepositoryProvider: Provider<PaymentRepository>,
) : MoleculeViewModel<PaymentEvent, PaymentUiState>(
    PaymentUiState.Loading,
    PaymentPresenter(paymentRepositoryProvider),
  )
