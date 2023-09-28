package com.hedvig.android.feature.home.home.ui

import com.hedvig.android.feature.home.di.GetHomeDataUseCaseProvider
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class HomeViewModel(
  getHomeDataUseCaseProvider: GetHomeDataUseCaseProvider,
  featureManager: FeatureManager,
) : MoleculeViewModel<HomeEvent, HomeUiState>(
  HomeUiState.Loading,
  HomePresenter(getHomeDataUseCaseProvider, featureManager),
)
