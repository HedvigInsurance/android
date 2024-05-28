package com.hedvig.android.feature.home.home.ui

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorage
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import kotlinx.coroutines.CoroutineScope

internal class HomeViewModel(
  getHomeDataUseCaseProvider: Provider<GetHomeDataUseCase>,
  chatLastMessageReadRepository: ChatLastMessageReadRepository,
  seenImportantMessagesStorage: SeenImportantMessagesStorage,
  crossSellCardNotificationBadgeServiceProvider: Provider<CrossSellCardNotificationBadgeService>,
  applicationScope: CoroutineScope,
) : MoleculeViewModel<HomeEvent, HomeUiState>(
    HomeUiState.Loading,
    HomePresenter(
      getHomeDataUseCaseProvider,
      chatLastMessageReadRepository,
      seenImportantMessagesStorage,
      crossSellCardNotificationBadgeServiceProvider,
      applicationScope,
    ),
  )
