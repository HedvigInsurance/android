package com.hedvig.feature.claim.chat

import com.hedvig.feature.claim.chat.data.StepContent

interface ConversationStep {
  val stepId: String
}

sealed class ConversationItem() {
  data class AudioRecording(val hint: String?, val uploadUri: String, val text: String, override val stepId: String) : ConversationItem(), ConversationStep
  data class AssistantMessage(val text: String, val subText: String, override val stepId: String) : ConversationItem(), ConversationStep
  data class AssistantLoadingState(val text: String, val subText: String, val isLoading: Boolean, override val stepId: String) : ConversationItem(), ConversationStep
  data class Form(val formFieldList: List<FormField>, override val stepId: String) : ConversationItem(), ConversationStep

  data class Summary(val items: List<StepContent.Summary.Item>, override val stepId: String) : ConversationItem(), ConversationStep
  data class Outcome(val claimId: String, override val stepId: String, val text: String) : ConversationItem(), ConversationStep
}

data class FormField(
  val fieldId: String,
  val type: FormFieldType,
  val defaultValue: String? = null,
  val currentValue: String? = null,
  val isRequired: Boolean = true,
  val minValue: String? = null,
  val maxValue: String? = null,
  val options: List<Pair<String, String>> = emptyList(),
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
