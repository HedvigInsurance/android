package com.hedvig.android.feature.connect.payment.trustly

import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallback
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class TrustlyPayoutViewModel(
  trustlyCallback: TrustlyCallback,
  startTrustlyPayoutSessionUseCase: StartTrustlyPayoutSessionUseCase,
  networkCacheManager: NetworkCacheManager,
) : MoleculeViewModel<TrustlyEvent, TrustlyUiState>(
    TrustlyUiState.Loading,
    TrustlyPayoutPresenter(
      trustlyCallback,
      startTrustlyPayoutSessionUseCase,
      networkCacheManager,
    ),
  )
