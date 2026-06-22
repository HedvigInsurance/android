package com.hedvig.android.feature.chat.inbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.feature.chat.data.GetAllConversationsUseCase
import com.hedvig.android.feature.chat.model.InboxConversation
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
internal class InboxViewModel(
  getAllConversationsUseCase: GetAllConversationsUseCase,
  featureManager: FeatureManager,
) : MoleculeViewModel<InboxEvent, InboxUiState>(
    initialState = InboxUiState.Loading,
    presenter = InboxPresenter(getAllConversationsUseCase, featureManager),
  )

internal class InboxPresenter(
  private val getAllConversationsUseCase: GetAllConversationsUseCase,
  private val featureManager: FeatureManager,
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
      combine(
        getAllConversationsUseCase.invoke(),
        featureManager.isFeatureEnabled(Feature.ENABLE_NEW_CONVERSATION_FROM_INBOX),
      ) { conversations, newChatButtonAvailable ->
        conversations to newChatButtonAvailable
      }.collectLatest { (conversations, newChatButtonAvailable) ->
        conversations.fold(
          ifLeft = {
            if (currentState is InboxUiState.Loading) {
              currentState = InboxUiState.Failure
            }
          },
          ifRight = { conversations ->
            currentState = InboxUiState.Success(
              conversations,
              newChatButtonAvailable,
            )
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
    val newConversationButtonAvailable: Boolean,
  ) : InboxUiState
}

internal sealed interface InboxEvent {
  data object Reload : InboxEvent
}
