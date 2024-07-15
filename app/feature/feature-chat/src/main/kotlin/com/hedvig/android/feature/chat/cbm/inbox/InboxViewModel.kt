package com.hedvig.android.feature.chat.cbm.inbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.chat.cbm.data.GetAllConversationsUseCase
import com.hedvig.android.feature.chat.cbm.model.InboxConversation
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest

internal class InboxViewModel(
  getAllConversationsUseCase: GetAllConversationsUseCase,
) : MoleculeViewModel<InboxEvent, InboxUiState>(
    initialState = InboxUiState.Loading,
    presenter = InboxPresenter(getAllConversationsUseCase),
  )

internal class InboxPresenter(
  private val getAllConversationsUseCase: GetAllConversationsUseCase,
) : MoleculePresenter<InboxEvent, InboxUiState> {
  @Composable
  override fun MoleculePresenterScope<InboxEvent>.present(lastState: InboxUiState): InboxUiState {
    var currentState by remember {
      mutableStateOf(lastState)
    }
    var loadIteration by remember {
      mutableIntStateOf(0)
    }

    CollectEvents { event ->
      when (event) {
        InboxEvent.Reload -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      if (currentState !is InboxUiState.Success) {
        currentState = InboxUiState.Loading
      }
      getAllConversationsUseCase.invoke().collectLatest { result ->
        result.fold(
          ifLeft = {
            currentState = InboxUiState.Failure
          },
          ifRight = { conversations ->
            currentState = InboxUiState.Success(conversations)
          },
        )
      }
    }
    return currentState
  }
}

internal sealed interface InboxUiState {
  data object Loading : InboxUiState

  data object Failure : InboxUiState

  data class Success(
    val inboxConversations: List<InboxConversation>,
  ) : InboxUiState
}

internal sealed interface InboxEvent {
  data object Reload : InboxEvent
}
