package com.hedvig.android.feature.chat.floating

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class FloatingBubbleViewModel(
  chatTooltipStorage: ChatTooltipStorage,
  chatLastMessageReadRepository: ChatLastMessageReadRepository,
  clock: Clock,
) : MoleculeViewModel<FloatingBubbleEvent, FloatingBubbleUiState>(
    FloatingBubbleUiState(false, false),
    FloatingBubblePresenter(chatTooltipStorage, chatLastMessageReadRepository, clock),
  )

private class FloatingBubblePresenter(
  private val chatTooltipStorage: ChatTooltipStorage,
  private val chatLastMessageReadRepository: ChatLastMessageReadRepository,
  private val clock: Clock,
) : MoleculePresenter<FloatingBubbleEvent, FloatingBubbleUiState> {
  @Composable
  override fun MoleculePresenterScope<FloatingBubbleEvent>.present(
    lastState: FloatingBubbleUiState,
  ): FloatingBubbleUiState {
    val showWelcomeTooltip = enoughDaysHavePassedSinceLastWelcomeTooltipShown(chatTooltipStorage, clock)
    val hasUnseenChatMessages by produceState(lastState.hasUnseenChatMessages) {
      while (isActive) {
        value = chatLastMessageReadRepository.isNewestMessageNewerThanLastReadTimestamp()
        delay(1.seconds) // todo(makerdays) Change to bigger number after demo
      }
    }
    CollectEvents { event ->
      when (event) {
        FloatingBubbleEvent.SeenTooltip -> {
          launch { chatTooltipStorage.setLastEpochDayWhenChatTooltipWasShown(clock.currentEpochDay) }
        }
      }
    }
    return FloatingBubbleUiState(showWelcomeTooltip, hasUnseenChatMessages)
  }
}

internal data class FloatingBubbleUiState(
  val showWelcomeTooltip: Boolean,
  val hasUnseenChatMessages: Boolean,
)

internal sealed interface FloatingBubbleEvent {
  object SeenTooltip : FloatingBubbleEvent
}

@Composable
private fun enoughDaysHavePassedSinceLastWelcomeTooltipShown(
  chatTooltipStorage: ChatTooltipStorage,
  clock: Clock,
): Boolean {
  val currentEpochDay = remember { clock.currentEpochDay }
  val lastEpochDayOpened by chatTooltipStorage.getLastEpochDayWhenChatTooltipWasShown().collectAsState(0)
  val diff = currentEpochDay - lastEpochDayOpened
  val enoughDaysHavePassedSinceLastTooltipShown = diff >= 30
  return enoughDaysHavePassedSinceLastTooltipShown
}

private val Clock.currentEpochDay
  get() = now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays().toLong()
