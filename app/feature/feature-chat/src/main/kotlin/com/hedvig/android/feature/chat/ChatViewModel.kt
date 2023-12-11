package com.hedvig.android.feature.chat

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.chat.data.ChatRepository
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.molecule.android.MoleculeViewModel
import kotlinx.datetime.Clock

internal class ChatViewModel(
  chatRepository: Provider<ChatRepository>,
  featureManager: FeatureManager,
  clock: Clock,
) : MoleculeViewModel<ChatEvent, ChatUiState>(
    ChatUiState.Initializing,
    ChatPresenter(
      chatRepository = chatRepository,
      featureManager = featureManager,
      clock = clock,
    ),
  )
