package com.hedvig.android.feature.importantmessages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.importantmessages.GetPublicImportantMessagesUseCase
import com.hedvig.android.data.importantmessages.PublicImportantMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class PublicImportantMessagesViewModel(
  getPublicImportantMessagesUseCase: GetPublicImportantMessagesUseCase,
) : MoleculeViewModel<PublicImportantMessagesEvent, PublicImportantMessagesUiState>(
    PublicImportantMessagesUiState(emptyList()),
    PublicImportantMessagesPresenter(getPublicImportantMessagesUseCase),
  )

internal class PublicImportantMessagesPresenter(
  private val getPublicImportantMessagesUseCase: GetPublicImportantMessagesUseCase,
) : MoleculePresenter<PublicImportantMessagesEvent, PublicImportantMessagesUiState> {
  @Composable
  override fun MoleculePresenterScope<PublicImportantMessagesEvent>.present(
    lastState: PublicImportantMessagesUiState,
  ): PublicImportantMessagesUiState {
    var messages by remember { mutableStateOf(lastState.messages) }
    var loadIteration by remember { mutableIntStateOf(0) }
    CollectEvents { event ->
      when (event) {
        PublicImportantMessagesEvent.Reload -> loadIteration++
      }
    }
    LaunchedEffect(loadIteration) {
      getPublicImportantMessagesUseCase.invoke().fold(
        ifLeft = { error -> logcat(LogPriority.INFO, error.throwable) { "Failed to load public important messages" } },
        ifRight = { messages = it },
      )
    }
    return PublicImportantMessagesUiState(messages)
  }
}

internal sealed interface PublicImportantMessagesEvent {
  data object Reload : PublicImportantMessagesEvent
}

internal data class PublicImportantMessagesUiState(val messages: List<PublicImportantMessage>)
