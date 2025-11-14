package com.hedvig.feature.claim.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConversationUiState(
  val conversation: List<ConversationStep> = emptyList(),
  val isInputActive: Boolean = true,
  val currentInputText: String = "",
  val claimIntent: ClaimIntent? = null,
  val errorMessage: String? = null,
)

sealed class UserAction {
  data class AudioRecordingSubmitted(val url: String) : UserAction()

  data class TextSubmitted(val text: String) : UserAction()

  data class FormSubmitted(val selectedValue: List<FormField>) : UserAction()

  object ErrorAcknowledged : UserAction()

  object SummarySubmitted : UserAction()
}

internal class ClaimChatViewModel(
  private val startClaimIntentUseCase: StartClaimIntentUseCase,
  private val getClaimIntentUseCase: GetClaimIntentUseCase,
  private val submitAudioRecordingUseCase: SubmitAudioRecordingUseCase,
  private val uploadAudioUseCase: UploadAudioUseCase,
  private val submitFormUseCase: SubmitFormUseCase,
  private val submitTaskUseCase: SubmitTaskUseCase,
  private val submitSummaryUseCase: SubmitSummaryUseCase,
) : ViewModel() {
  private var pollingJob: Job? = null
  private val scope = CoroutineScope(Dispatchers.Default)
  private val _state = MutableStateFlow(ConversationUiState())
  val state: StateFlow<ConversationUiState> = _state.asStateFlow()

  init {
    startClaimIntent()
  }

  private fun startClaimIntent() = scope.launch {
    startClaimIntentUseCase.invoke(sourceMessageId = "2ec77791-0705-4bb9-9689-80ba3aed7202").fold(
      ifLeft = { errorMessage ->
        _state.update { it.copy(errorMessage = errorMessage.message) }
      },
      ifRight = { claimIntent ->
        updateConversationStateWithNewClaimIntent(claimIntent)
        if (claimIntent.step.stepContent is StepContent.Task) {
          pollIntentUntilCompletedAndSubmit(claimIntent.id)
        }
      },
    )
  }

  private fun pollIntentUntilCompletedAndSubmit(claimIntentId: String) = scope.launch {
    pollingJob?.cancel()
    pollingJob = getClaimIntentUseCase.invoke(claimIntentId)
      .onEach { eitherResult ->
        eitherResult.fold(
          ifLeft = { errorMessage ->
            _state.update { it.copy(errorMessage = errorMessage.message) }
          },
          ifRight = { claimIntent ->
            updateConversationStateWithNewClaimIntent(claimIntent)
            if (claimIntent.step.stepContent is StepContent.Task) {
              if (claimIntent.step.stepContent.isCompleted) {
                submitTask(claimIntent.step.id)
              }
            }
          },
        )
      }
      .takeWhile { eitherResult ->
        eitherResult.fold(
          ifLeft = { errorMessage ->
            _state.update { it.copy(errorMessage = errorMessage.message) }
            return@takeWhile false
          },
          ifRight = { claimIntent ->
            updateConversationStateWithNewClaimIntent(claimIntent)
            if (claimIntent.step.stepContent is StepContent.Task) {
              return@takeWhile !claimIntent.step.stepContent.isCompleted
            }
            return@takeWhile true
          },
        )
      }
      .launchIn(viewModelScope)
  }

  fun processUserAction(action: UserAction) {
    when (action) {
      is UserAction.AudioRecordingSubmitted -> handleAudioRecordingSubmission(action.url)
      is UserAction.TextSubmitted -> handleTextSubmission(action.text)
      is UserAction.FormSubmitted -> handleFormSubmission(action.selectedValue)
      is UserAction.ErrorAcknowledged -> handleErrorAcknowledged()
      UserAction.SummarySubmitted -> handleSummarySubmitted()
    }
  }

  private fun handleSummarySubmitted() = scope.launch {
    val stepId = state.value.claimIntent?.step?.id
    stepId?.let {
      submitSummaryUseCase.invoke(it).fold(
        ifLeft = { errorMessage ->
          _state.update { it.copy(errorMessage = errorMessage.message) }
        },
        ifRight = { claimIntent ->
          updateConversationStateWithNewClaimIntent(claimIntent)
          if (claimIntent.step.stepContent is StepContent.Task) {
            pollIntentUntilCompletedAndSubmit(claimIntent.id)
          }
        },
      )
    }
  }

  private fun handleErrorAcknowledged() {
    _state.update { it.copy(errorMessage = null) }
  }

  private fun handleAudioRecordingSubmission(uploadUrl: String) = scope.launch {
    val stepId = state.value.claimIntent?.step?.id
    if (stepId != null) {
      submitAudioRecordingUseCase.invoke(
        stepId = stepId,
        audioFileId = uploadUrl,
        freeText = null,
      ).fold(
        ifLeft = { errorMessage ->
          _state.update { it.copy(errorMessage = errorMessage.message) }
        },
        ifRight = { claimIntent ->
          updateConversationStateWithNewClaimIntent(claimIntent)
          submitTask(claimIntent.step.id)
        },
      )
    }
  }

  private fun submitTask(stepId: String) = scope.launch {
    submitTaskUseCase.invoke(stepId)
      .fold(
        ifLeft = { errorMessage ->
          _state.update { it.copy(errorMessage = errorMessage.message) }
        },
        ifRight = { claimIntent ->
          updateConversationStateWithNewClaimIntent(claimIntent)
        },
      )
  }

  private fun handleTextSubmission(text: String) = scope.launch {
  }

  private fun handleFormSubmission(selectedValue: List<FormField>) = scope.launch {
    val stepId = state.value.claimIntent?.step?.id
    if (stepId != null) {
      submitFormUseCase.invoke(stepId, selectedValue).fold(
        ifLeft = { errorMessage ->
          _state.update { it.copy(errorMessage = errorMessage.message) }
        },
        ifRight = { claimIntent ->
          updateConversationStateWithNewClaimIntent(claimIntent)
          if (claimIntent.step.stepContent is StepContent.Task) {
            pollIntentUntilCompletedAndSubmit(claimIntent.id)
          }
        },
      )
    }
  }

  fun updateConversationStateWithNewClaimIntent(claimIntent: ClaimIntent) {
    _state.update { currentState ->
      val existingConversationStep = currentState.conversation.find { it.stepId == claimIntent.step.id }
      if (existingConversationStep == null) {
        currentState.copy(
          conversation = currentState.conversation + claimIntent.createConversationItem(),
          claimIntent = claimIntent,
        )
      } else {
        val updatedConversation = currentState.conversation.map { item ->
          if (item.stepId == claimIntent.step.id) {
            return@map claimIntent.createConversationItem()
          }
          item
        }
        currentState.copy(
          conversation = updatedConversation,
          claimIntent = claimIntent,
        )
      }
    }
  }
}
