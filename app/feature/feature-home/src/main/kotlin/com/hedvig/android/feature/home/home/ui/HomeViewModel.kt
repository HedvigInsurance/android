package com.hedvig.android.feature.home.home.ui

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.cross.sell.after.claim.closed.CrossSellAfterClaimClosedRepository
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorage
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import kotlinx.coroutines.CoroutineScope

internal class HomeViewModel(
  getHomeDataUseCaseProvider: Provider<GetHomeDataUseCase>,
  seenImportantMessagesStorage: SeenImportantMessagesStorage,
  crossSellCardNotificationBadgeServiceProvider: Provider<CrossSellCardNotificationBadgeService>,
  crossSellAfterClaimClosedRepository: CrossSellAfterClaimClosedRepository,
  applicationScope: CoroutineScope,
) : MoleculeViewModel<HomeEvent, HomeUiState>(
    HomeUiState.Loading,
    HomePresenter(
      getHomeDataUseCaseProvider,
      seenImportantMessagesStorage,
      crossSellCardNotificationBadgeServiceProvider,
      crossSellAfterClaimClosedRepository,
      applicationScope,
    ),
  )
