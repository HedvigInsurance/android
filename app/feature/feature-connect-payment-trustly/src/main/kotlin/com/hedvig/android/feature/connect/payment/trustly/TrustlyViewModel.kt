package com.hedvig.android.feature.connect.payment.trustly

import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallback
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
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
