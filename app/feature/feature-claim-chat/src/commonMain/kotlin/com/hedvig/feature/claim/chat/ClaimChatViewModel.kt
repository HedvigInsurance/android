package com.hedvig.feature.claim.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.feature.claim.chat.data.ClaimIntent
import com.hedvig.feature.claim.chat.data.GetClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.StartClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.SubmitAudioRecordingUseCase
import com.hedvig.feature.claim.chat.data.SubmitFormUseCase
import com.hedvig.feature.claim.chat.data.SubmitSummaryUseCase
import com.hedvig.feature.claim.chat.data.SubmitTaskUseCase
import com.hedvig.feature.claim.chat.data.UploadAudioUseCase
import com.hedvig.feature.claim.chat.data.createConversationItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

data class ClaimChatUiState(
  val conversation: List<ConversationStep> = emptyList(),
  val isInputActive: Boolean = true,
  val currentInputText: String = "",
  val claimIntent: ClaimIntent? = null,
  val errorMessage: String? = null,
)

sealed class ClaimChatEvent {
  data class AudioRecordingSubmitted(val url: String) : ClaimChatEvent()
  data class TextSubmitted(val text: String) : ClaimChatEvent()
  data class FormSubmitted(val selectedValue: List<FormField>) : ClaimChatEvent()
  object ErrorAcknowledged : ClaimChatEvent()
  object SummarySubmitted : ClaimChatEvent()
}

internal class ClaimChatViewModel(
  isDevelopmentFlow: Boolean,
  messageId: String?,
  startClaimIntentUseCase: StartClaimIntentUseCase,
  getClaimIntentUseCase: GetClaimIntentUseCase,
  submitAudioRecordingUseCase: SubmitAudioRecordingUseCase,
  uploadAudioUseCase: UploadAudioUseCase,
  submitFormUseCase: SubmitFormUseCase,
  submitTaskUseCase: SubmitTaskUseCase,
  submitSummaryUseCase: SubmitSummaryUseCase,
) : MoleculeViewModel<ClaimChatEvent, ClaimChatUiState>(
  ClaimChatUiState(),
  ClaimChatPresenter(
    isDevelopmentFlow = isDevelopmentFlow,
    messageId = messageId,
    startClaimIntentUseCase = startClaimIntentUseCase,
    getClaimIntentUseCase = getClaimIntentUseCase,
    submitAudioRecordingUseCase = submitAudioRecordingUseCase,
    uploadAudioUseCase = uploadAudioUseCase,
    submitFormUseCase = submitFormUseCase,
    submitTaskUseCase = submitTaskUseCase,
    submitSummaryUseCase = submitSummaryUseCase,
  ),
)

private class ClaimChatPresenter(
  private val isDevelopmentFlow: Boolean,
  private val messageId: String?,
  private val startClaimIntentUseCase: StartClaimIntentUseCase,
  private val getClaimIntentUseCase: GetClaimIntentUseCase,
  private val submitAudioRecordingUseCase: SubmitAudioRecordingUseCase,
  private val uploadAudioUseCase: UploadAudioUseCase,
  private val submitFormUseCase: SubmitFormUseCase,
  private val submitTaskUseCase: SubmitTaskUseCase,
  private val submitSummaryUseCase: SubmitSummaryUseCase,
) : MoleculePresenter<ClaimChatEvent, ClaimChatUiState> {
  @Composable
  override fun MoleculePresenterScope<ClaimChatEvent>.present(
    lastState: ClaimChatUiState,
  ): ClaimChatUiState {

    var currentState by remember { mutableStateOf(lastState) }
    var pollingJob: Job? by remember { mutableStateOf(null) }
    var audioRecordingUrl: String? by remember { mutableStateOf(null) }
    var selectedFormField: List<FormField>? by remember { mutableStateOf(null) }
    var hasSummaryToSubmit: Boolean by remember { mutableStateOf(false) }

    CollectEvents { event ->
      when (event) {
        is ClaimChatEvent.AudioRecordingSubmitted -> {
          audioRecordingUrl = event.url
        }


        is ClaimChatEvent.TextSubmitted -> {
          // todo
        }

        is ClaimChatEvent.FormSubmitted -> {
          selectedFormField = event.selectedValue
        }

        is ClaimChatEvent.ErrorAcknowledged -> {
          currentState = currentState.copy(errorMessage = null)
        }

        ClaimChatEvent.SummarySubmitted -> {
          hasSummaryToSubmit = true
        }
      }
    }

    LaunchedEffect(Unit) {
      startClaimIntentUseCase.invoke(
        sourceMessageId = messageId,
        developmentFlow = isDevelopmentFlow,
      ).fold(
        ifLeft = { errorMessage ->
          currentState = currentState.copy(errorMessage = errorMessage.message)
        },
        ifRight = { claimIntent ->
          currentState = updateConversationStateWithNewClaimIntent(currentState, claimIntent)
          if (claimIntent.step.stepContent is StepContent.Task) {
            pollingJob?.cancel()
            pollingJob = pollIntentUntilCompletedAndSubmit(
              claimIntent.id,
              this,
              { currentState },
            ) { newState ->
              currentState = newState
            }
          }
        },
      )
    }

    LaunchedEffect(audioRecordingUrl) {
      val url = audioRecordingUrl ?: return@LaunchedEffect
      val stepId = currentState.claimIntent?.step?.id ?: return@LaunchedEffect
      submitAudioRecordingUseCase.invoke(
        stepId = stepId,
        audioFileId = url,
        freeText = null,
      ).fold(
        ifLeft = { errorMessage ->
          currentState = currentState.copy(errorMessage = errorMessage.message)
        },
        ifRight = { claimIntent ->
          val capturedState = updateConversationStateWithNewClaimIntent(currentState, claimIntent)
          submitTask(
            claimIntent.step.id,
            { capturedState },
          ) { finalState ->
            currentState = finalState
            pollingJob = null
          }
        },
      )
    }

    LaunchedEffect(selectedFormField) {
      val selectedValue = selectedFormField ?: return@LaunchedEffect
      val stepId = currentState.claimIntent?.step?.id ?: return@LaunchedEffect
      submitFormUseCase.invoke(stepId, selectedValue).fold(
        ifLeft = { errorMessage ->
          currentState = currentState.copy(errorMessage = errorMessage.message)
          pollingJob = null
        },
        ifRight = { claimIntent ->
          val newState = updateConversationStateWithNewClaimIntent(currentState, claimIntent)
          val newPollingJob = if (claimIntent.step.stepContent is StepContent.Task) {
            pollIntentUntilCompletedAndSubmit(
              claimIntent.id,
              this,
              { newState },
            ) { state ->
              currentState = newState
              pollingJob = null
            }
          } else {
            null
          }
          currentState = newState
          pollingJob = newPollingJob
        },
      )
    }

    LaunchedEffect(hasSummaryToSubmit) {

      if (!hasSummaryToSubmit) return@LaunchedEffect
      val stepId = currentState.claimIntent?.step?.id ?: return@LaunchedEffect

      submitSummaryUseCase.invoke(stepId).fold(
        ifLeft = { errorMessage ->
          currentState = currentState.copy(errorMessage = errorMessage.message)
          pollingJob = null
        },
        ifRight = { claimIntent ->
          val newState = updateConversationStateWithNewClaimIntent(currentState, claimIntent)
          val newPollingJob = if (claimIntent.step.stepContent is StepContent.Task) {
            pollIntentUntilCompletedAndSubmit(
              claimIntent.id,
              this,
              { newState },
            ) { state ->
              currentState = state
            }
          } else {
            null
          }
          currentState = newState
          pollingJob = newPollingJob
        },
      )
    }

    return currentState
  }

  private fun pollIntentUntilCompletedAndSubmit(
    claimIntentId: String,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    getCurrentState: () -> ClaimChatUiState,
    updateState: (ClaimChatUiState) -> Unit,
  ): Job {
    return getClaimIntentUseCase.invoke(claimIntentId)
      .onEach { eitherResult ->
        eitherResult.fold(
          ifLeft = { errorMessage ->
            updateState(getCurrentState().copy(errorMessage = errorMessage.message))
          },
          ifRight = { claimIntent ->
            val newState = updateConversationStateWithNewClaimIntent(getCurrentState(), claimIntent)
            updateState(newState)
            if (claimIntent.step.stepContent is StepContent.Task) {
              if (claimIntent.step.stepContent.isCompleted) {
                coroutineScope.launch {
                  submitTask(claimIntent.step.id, getCurrentState, updateState)
                }
              }
            }
          },
        )
      }
      .takeWhile { eitherResult ->
        eitherResult.fold(
          ifLeft = { false },
          ifRight = { claimIntent ->
            if (claimIntent.step.stepContent is StepContent.Task) {
              !claimIntent.step.stepContent.isCompleted
            } else {
              true
            }
          },
        )
      }
      .launchIn(coroutineScope)
  }

  private suspend fun submitTask(
    stepId: String,
    getCurrentState: () -> ClaimChatUiState,
    updateState: (ClaimChatUiState) -> Unit,
  ) {
    submitTaskUseCase.invoke(stepId).fold(
      ifLeft = { errorMessage ->
        updateState(getCurrentState().copy(errorMessage = errorMessage.message))
      },
      ifRight = { claimIntent ->
        updateState(updateConversationStateWithNewClaimIntent(getCurrentState(), claimIntent))
      },
    )
  }

  private fun updateConversationStateWithNewClaimIntent(
    currentState: ClaimChatUiState,
    claimIntent: ClaimIntent,
  ): ClaimChatUiState {
    val existingConversationStep = currentState.conversation.find { it.stepId == claimIntent.step.id }
    return if (existingConversationStep == null) {
      currentState.copy(
        conversation = currentState.conversation + claimIntent.createConversationItem(),
        claimIntent = claimIntent,
      )
    } else {
      val updatedConversation = currentState.conversation.map { item ->
        if (item.stepId == claimIntent.step.id) {
          claimIntent.createConversationItem()
        } else {
          item
        }
      }
      currentState.copy(
        conversation = updatedConversation,
        claimIntent = claimIntent,
      )
    }
  }
}
