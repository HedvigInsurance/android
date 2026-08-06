package com.hedvig.android.feature.home.home.ui

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.crosssells.CrossSellImpressionTracker
import com.hedvig.android.data.claimintent.DeleteClaimIntentDraftUseCase
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorage
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationService
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class HomeViewModel(
  getHomeDataUseCase: GetHomeDataUseCase,
  seenImportantMessagesStorage: SeenImportantMessagesStorage,
  crossSellHomeNotificationService: CrossSellHomeNotificationService,
  applicationScope: ApplicationScope,
  hedvigBuildConstants: HedvigBuildConstants,
  deleteClaimIntentDraftUseCase: DeleteClaimIntentDraftUseCase,
  crossSellImpressionTracker: CrossSellImpressionTracker,
) : MoleculeViewModel<HomeEvent, HomeUiState>(
    HomeUiState.Loading,
    HomePresenter(
      getHomeDataUseCase,
      seenImportantMessagesStorage,
      crossSellHomeNotificationService,
      applicationScope,
      hedvigBuildConstants.isProduction,
      deleteClaimIntentDraftUseCase,
      crossSellImpressionTracker,
    ),
  )
