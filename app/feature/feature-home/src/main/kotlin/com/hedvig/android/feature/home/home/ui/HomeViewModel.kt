package com.hedvig.android.feature.home.home.ui

import androidx.lifecycle.ViewModel
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorage
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationService
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Inject
@ViewModelKey
@ContributesIntoMap(ActivityRetainedScope::class, binding<ViewModel>())
internal class HomeViewModel(
  getHomeDataUseCaseProvider: Provider<GetHomeDataUseCase>,
  seenImportantMessagesStorage: SeenImportantMessagesStorage,
  crossSellHomeNotificationServiceProvider: Provider<CrossSellHomeNotificationService>,
  applicationScope: ApplicationScope,
  hedvigBuildConstants: HedvigBuildConstants,
) : MoleculeViewModel<HomeEvent, HomeUiState>(
    HomeUiState.Loading,
    HomePresenter(
      getHomeDataUseCaseProvider,
      seenImportantMessagesStorage,
      crossSellHomeNotificationServiceProvider,
      applicationScope,
      hedvigBuildConstants.isProduction,
    ),
  )
