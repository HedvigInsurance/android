package com.hedvig.android.feature.home.home.ui

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.ImportantMessagesSeer
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class HomeViewModel(
  getHomeDataUseCaseProvider: Provider<GetHomeDataUseCase>,
  chatLastMessageReadRepository: ChatLastMessageReadRepository,
  importantMessagesSeer: ImportantMessagesSeer,
) : MoleculeViewModel<HomeEvent, HomeUiState>(
    HomeUiState.Loading,
    HomePresenter(getHomeDataUseCaseProvider, chatLastMessageReadRepository, importantMessagesSeer),
  )
