package com.hedvig.android.feature.payments2

import com.hedvig.android.molecule.android.MoleculeViewModel

internal class PaymentOverviewViewModel : MoleculeViewModel<PaymentEvent, OverViewUiState>(
  OverViewUiState.Loading,
  PaymentOverviewPresenter(),
)
