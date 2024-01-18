package com.hedvig.android.feature.home.home.ui

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class HomeViewModel(
    getHomeDataUseCaseProvider: Provider<GetHomeDataUseCase>,
    chatLastMessageReadRepository: ChatLastMessageReadRepository,
    featureManager: FeatureManager,
) : MoleculeViewModel<HomeEvent, HomeUiState>(
    HomeUiState.Loading,
    HomePresenter(getHomeDataUseCaseProvider, chatLastMessageReadRepository, featureManager),
  )
