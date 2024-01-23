package com.hedvig.android.feature.chat

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.chat.data.ChatRepository
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.navigation.core.AppDestination
import kotlinx.datetime.Clock

internal class ChatViewModel(
  chatRepository: Provider<ChatRepository>,
  clock: Clock,
  chatContext: AppDestination.Chat.ChatContext?
) : MoleculeViewModel<ChatEvent, ChatUiState>(
    ChatUiState.Initializing,
    ChatPresenter(
      chatRepository = chatRepository,
      clock = clock,
      chatContext = chatContext,
    ),
  )
