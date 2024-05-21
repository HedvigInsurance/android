package com.hedvig.android.data.chat.icon

import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive

interface GetChatIconAppStateUseCase {
  fun invoke(): Flow<ChatIconAppState>
}

internal class GetChatIconAppStateUseCaseImpl(
  private val settingsDataStore: SettingsDataStore,
  private val featureManager: FeatureManager,
  private val shouldShowChatIconUseCase: ShouldShowChatIconUseCase,
  private val chatLastMessageReadRepository: ChatLastMessageReadRepository,
) : GetChatIconAppStateUseCase {
  override fun invoke(): Flow<ChatIconAppState> {
    return shouldShowChatIconUseCase.invoke().flatMapLatest { showChat: Boolean ->
      if (!showChat) {
        flowOf(ChatIconAppState.Hidden)
      } else {
        combine(
          settingsDataStore.chatBubbleSetting(),
          featureManager.isFeatureEnabled(Feature.CHAT_BUBBLE),
          chatLastMessageReadRepository.pollingIsNewestMessageNewerThanLastReadTimestamp(),
        ) { userPreferenceHasChatBubbleEnabled, isChatBubbleFeatureEnabled, hasNotification ->
          val showAsFloatingBubble = userPreferenceHasChatBubbleEnabled && isChatBubbleFeatureEnabled
          ChatIconAppState.Shown(showAsFloatingBubble, hasNotification)
        }
      }
    }
  }

  private fun ChatLastMessageReadRepository.pollingIsNewestMessageNewerThanLastReadTimestamp(): Flow<Boolean> {
    return flow {
      while (currentCoroutineContext().isActive) {
        emit(isNewestMessageNewerThanLastReadTimestamp())
        delay(5.seconds)
      }
    }
  }
}

sealed interface ChatIconAppState {
  object Hidden : ChatIconAppState

  data class Shown(
    val showAsFloatingBubble: Boolean,
    val hasNotification: Boolean,
  ) : ChatIconAppState
}
