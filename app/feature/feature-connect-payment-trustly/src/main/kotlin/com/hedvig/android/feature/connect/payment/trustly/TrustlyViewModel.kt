package com.hedvig.android.feature.connect.payment.trustly

import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallback
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class TrustlyViewModel(
  trustlyCallback: TrustlyCallback,
  startTrustlySessionUseCase: StartTrustlySessionUseCase,
  networkCacheManager: NetworkCacheManager,
) : MoleculeViewModel<TrustlyEvent, TrustlyUiState>(
    TrustlyUiState.Loading,
    TrustlyPresenter(
      trustlyCallback,
      startTrustlySessionUseCase,
      networkCacheManager,
    ),
  )
