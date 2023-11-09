package com.hedvig.android.feature.connect.payment.adyen

import com.hedvig.android.feature.connect.payment.adyen.data.GetAdyenPaymentUrlUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class AdyenViewModel(
  getAdyenPaymentUrlUseCase: GetAdyenPaymentUrlUseCase,
) : MoleculeViewModel<AdyenEvent, AdyenUiState>(
    AdyenUiState.Loading,
    AdyenPresenter(getAdyenPaymentUrlUseCase),
  )
