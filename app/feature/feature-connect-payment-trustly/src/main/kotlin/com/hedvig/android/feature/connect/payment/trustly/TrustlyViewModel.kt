package com.hedvig.android.feature.connect.payment.trustly

import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallback
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class TrustlyViewModel(
  trustlyCallback: TrustlyCallback,
  startTrustlySessionUseCase: StartTrustlySessionUseCase,
) : MoleculeViewModel<TrustlyEvent, TrustlyUiState>(
  TrustlyUiState.Loading,
  TrustlyPresenter(
    trustlyCallback,
    startTrustlySessionUseCase,
  ),
)
