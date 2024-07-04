package com.hedvig.android.feature.chat.cbm

import androidx.compose.runtime.Composable
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.Clock

internal class CbmChatViewModel(
  chatRepository: Provider<CbmChatRepository>,
  clock: Clock,
) : MoleculeViewModel<CbmChatEvent, CbmChatUiState>(
    CbmChatUiState.Initializing,
    CbmChatPresenter(
      chatRepository = chatRepository,
      clock = clock,
    ),
  )

internal class CbmChatPresenter(
  chatRepository: Provider<CbmChatRepository>,
  clock: Clock,
) : MoleculePresenter<CbmChatEvent, CbmChatUiState> {
  @Composable
  override fun MoleculePresenterScope<CbmChatEvent>.present(lastState: CbmChatUiState): CbmChatUiState {
    return lastState
  }
}

internal class CbmChatRepository

internal sealed interface CbmChatEvent

internal sealed interface CbmChatUiState {
  data object Initializing : CbmChatUiState
}
