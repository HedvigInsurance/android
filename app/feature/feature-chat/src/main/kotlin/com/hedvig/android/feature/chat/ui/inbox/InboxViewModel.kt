package com.hedvig.android.feature.chat.ui.inbox

import androidx.compose.runtime.Composable
import com.hedvig.android.feature.chat.model.Conversation
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class InboxViewModel : MoleculeViewModel<InboxEvent, InboxUiState>(
  initialState = InboxUiState.Loading,
  presenter = InboxPresenter(),
)

internal class InboxPresenter : MoleculePresenter<InboxEvent, InboxUiState> {
  @Composable
  override fun MoleculePresenterScope<InboxEvent>.present(lastState: InboxUiState): InboxUiState {
    TODO("Not yet implemented")
  }
}

internal sealed interface InboxUiState {
  data object Loading : InboxUiState

  data object Failure : InboxUiState

  data class Success(
    val conversations: List<Conversation>,
  ) : InboxUiState
}

internal sealed interface InboxEvent {
  data object Reload : InboxEvent
}
