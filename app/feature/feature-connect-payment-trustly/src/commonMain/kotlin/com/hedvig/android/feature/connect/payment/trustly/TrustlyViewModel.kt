package com.hedvig.android.feature.connect.payment.trustly

import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallback
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
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
