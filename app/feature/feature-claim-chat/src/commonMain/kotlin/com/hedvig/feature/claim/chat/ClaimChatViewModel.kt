package com.hedvig.feature.claim.chat

import androidx.lifecycle.ViewModel
import com.hedvig.feature.claim.chat.audiorecorder.AudioRecorderUiState
import com.hedvig.feature.claim.chat.data.GetClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.StartClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.SubmitAudioRecordingUseCase
import com.hedvig.feature.claim.chat.data.SubmitFormUseCase
import com.hedvig.feature.claim.chat.data.SubmitSummaryUseCase
import com.hedvig.feature.claim.chat.data.SubmitTaskUseCase
import com.hedvig.feature.claim.chat.data.UploadAudioUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConversationUiState(
  val conversation: List<ConversationItem> = emptyList(),
  val isInputActive: Boolean = true, // We start with the ability to type
  val currentInputText: String = "",
)

sealed class UserAction {
  data class AudioRecordingSubmitted(val url: String) : UserAction()
  data class TextSubmitted(val text: String) : UserAction()
  data class FormSubmitted(val selectedValue: String) : UserAction()
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

  private val scope = CoroutineScope(Dispatchers.Default)

  private val _state = MutableStateFlow(ConversationUiState())
  val state: StateFlow<ConversationUiState> = _state.asStateFlow()

  init {
    scope.launch {
      _state.update {
        it.copy(
          conversation = listOf(
            ConversationItem.AssistantMessage(
              text = "Hello.",
              subText = "How can we help you?",
            ),
          ),
        )
      }

      delay(500)

      _state.update {
        it.copy(
          conversation = it.conversation +
            ConversationItem.AudioRecording(
              AudioRecorderUiState.AudioRecording.NotRecording,
            ),
        )
      }
    }
  }

  fun processUserAction(action: UserAction) {
    when (action) {
      is UserAction.AudioRecordingSubmitted -> handleAudioRecordingSubmission()
      is UserAction.TextSubmitted -> handleTextSubmission(action.text)
      is UserAction.FormSubmitted -> handleFormSubmission(action.selectedValue)
    }
  }

  private fun handleAudioRecordingSubmission() = scope.launch {

  }

  private fun handleTextSubmission(text: String) = scope.launch {
    _state.update {
      it.copy(
        isInputActive = false,
        currentInputText = "",
        conversation = it.conversation + ConversationItem.UserMessage(text),
      )
    }

    delay(500)

    val actionFormFields = listOf(
      FormField(
        id = "scan_receipt",
        type = FormFieldType.BINARY,
        title = "Scan receipt",
        options = listOf("I dont have it", "Scan receipt"),
      ),
    )

    val actionForm = ConversationItem.Form(actionFormFields)

    _state.update {
      it.copy(
        conversation = it.conversation + ConversationItem.AssistantMessage(
          text = "Ok, I see.",
          subText = "If you have a receipt, please scan it and we'll sort this out.",
        ) + actionForm,
      )
    }
  }

  private fun handleFormSubmission(selectedValue: String) = scope.launch {
    _state.update {
      it.copy(
        conversation = it.conversation + ConversationItem.AssistantLoadingState(
          text = "Perfect, thanks.",
          subText = "Scanning the market value for AirPods Pro...",
          isLoading = true,
        ),
      )
    }

    delay(3000)

    _state.update {
      val updatedList = it.conversation.map { item ->
        if (item is ConversationItem.AssistantLoadingState) {
          item.copy(
            subText = "Hang on while we're checking the market value.",
            isLoading = false,
          )
        } else {
          item
        }
      }
      it.copy(conversation = updatedList)
    }

    delay(1000)

    val compensationForm = ConversationItem.Form(
      listOf(
        FormField(
          id = "compensation_choice",
          type = FormFieldType.SINGLE_SELECT,
          options = listOf(
            "1 250 kr instant payout with Swish",
            "Refurbished pair",
          ),
          title = "See old ones for a refurbished",
        ),
      ),
    )

    _state.update {
      it.copy(
        conversation = it.conversation +
          ConversationItem.AssistantMessage(
            text = "We can compensate you with \n1 250 kr or you can trade in your old ones for a refurbished pair.",
            subText = "",
          ) +
          compensationForm,
        isInputActive = true,
      )
    }
  }
}
