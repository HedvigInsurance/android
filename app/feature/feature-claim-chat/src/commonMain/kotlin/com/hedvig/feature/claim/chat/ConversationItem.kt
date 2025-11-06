package com.hedvig.feature.claim.chat

import com.hedvig.feature.claim.chat.audiorecorder.AudioRecorderUiState

sealed class ConversationItem() {
  data class AudioRecording(val uiState: AudioRecorderUiState, val hint: String?, val uploadUri: String) : ConversationItem()
  data class UserMessage(val text: String) : ConversationItem()
  data class AssistantMessage(val text: String, val subText: String) : ConversationItem()
  data class AssistantLoadingState(val text: String, val subText: String, val isLoading: Boolean) : ConversationItem()
  data class Form(val formFieldList: List<FormField>) : ConversationItem()
}

data class FormField(
  val id: String,
  val type: FormFieldType,
  val defaultValue: String? = null,
  val currentValue: String? = null,
  val isRequired: Boolean = true,
  val minValue: String? = null,
  val maxValue: String? = null,
  val options: List<String> = emptyList(),
  val suffix: String? = null,
  val title: String? = null,
)

enum class FormFieldType {
  TEXT,
  DATE,
  NUMBER,
  SINGLE_SELECT,
  BINARY,
}
