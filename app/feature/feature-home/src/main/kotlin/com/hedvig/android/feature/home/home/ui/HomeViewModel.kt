package com.hedvig.android.feature.home.home.ui

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorage
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationService
import kotlinx.coroutines.CoroutineScope

internal class HomeViewModel(
  getHomeDataUseCaseProvider: Provider<GetHomeDataUseCase>,
  seenImportantMessagesStorage: SeenImportantMessagesStorage,
  crossSellHomeNotificationServiceProvider: Provider<CrossSellHomeNotificationService>,
  applicationScope: CoroutineScope,
) : MoleculeViewModel<HomeEvent, HomeUiState>(
    HomeUiState.Loading,
    HomePresenter(
      getHomeDataUseCaseProvider,
      seenImportantMessagesStorage,
      crossSellHomeNotificationServiceProvider,
      applicationScope,
    ),
  )
