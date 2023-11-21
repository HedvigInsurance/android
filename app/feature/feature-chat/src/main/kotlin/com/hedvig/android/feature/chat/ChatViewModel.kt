package com.hedvig.android.feature.chat

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.chat.closedevent.ChatClosedEventStore
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class ChatViewModel(
  chatRepository: Provider<ChatRepository>,
  chatClosedTracker: ChatClosedEventStore,
  featureManager: FeatureManager,
  demoManager: DemoManager,
) : MoleculeViewModel<ChatEventNew, ChatUiState>(
    ChatUiState.Initializing,
    ChatPresenter(
      chatRepository = chatRepository,
      chatClosedTracker = chatClosedTracker,
      featureManager = featureManager,
      demoManager = demoManager,
    ),
  )
