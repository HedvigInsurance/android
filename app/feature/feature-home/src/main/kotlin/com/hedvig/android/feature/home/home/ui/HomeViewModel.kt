package com.hedvig.android.feature.home.home.ui

import com.hedvig.android.feature.home.data.GetHomeDataUseCase
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class HomeViewModel(
  getHomeDataUseCase: GetHomeDataUseCase,
  featureManager: FeatureManager,
) : MoleculeViewModel<HomeEvent, HomeUiState>(
  HomeUiState.Loading,
  HomePresenter(getHomeDataUseCase, featureManager),
)
