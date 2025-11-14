package com.hedvig.feature.claim.chat.data

import com.hedvig.feature.claim.chat.ConversationItem
import com.hedvig.feature.claim.chat.ConversationItem.AssistantLoadingState
import com.hedvig.feature.claim.chat.ConversationItem.AssistantMessage
import com.hedvig.feature.claim.chat.ConversationItem.AudioRecording
import com.hedvig.feature.claim.chat.ConversationItem.Form
import com.hedvig.feature.claim.chat.ConversationItem.Summary
import com.hedvig.feature.claim.chat.ConversationStep
import com.hedvig.feature.claim.chat.FormField
import com.hedvig.feature.claim.chat.FormFieldType
import octopus.fragment.AudioRecordingFragment
import octopus.fragment.ClaimIntentFragment
import octopus.fragment.ClaimIntentStepContentFragment
import octopus.fragment.FormFragment
import octopus.fragment.OutcomeFragment
import octopus.fragment.SummaryFragment
import octopus.fragment.TaskFragment


fun ClaimIntentFragment.CurrentStep.toClaimIntentStep(): ClaimIntentStep {
  return ClaimIntentStep(
    id = id,
    text = text,
    stepContent = this.content.toStepContent(),
  )
}

private fun ClaimIntentStepContentFragment.toStepContent(): StepContent {
  return when (this) {
    is AudioRecordingFragment -> StepContent.AudioRecording(hint, uploadUri)
    is FormFragment -> StepContent.Form(this.fields.toFields())
    is TaskFragment -> StepContent.Task(description, isCompleted)
    is SummaryFragment -> StepContent.Summary(items.map { StepContent.Summary.Item(it.title, it.value) })
    is OutcomeFragment -> StepContent.Outcome(claimId)
    else -> StepContent.Unknown
  }
}

private fun List<FormFragment.Field>.toFields(): List<StepContent.Form.Field> {
  return this.map { field ->
    StepContent.Form.Field(
      id = field.id,
      isRequired = field.isRequired,
      suffix = field.suffix,
      title = field.title,
      defaultValue = field.defaultValue,
      maxValue = field.maxValue,
      minValue = field.minValue,
      type = field.type.toString(), // todo
      options = field.options?.map { it.title to it.value } ?: emptyList()
    )
  }
}

fun ClaimIntent.createConversationItem(): ConversationStep = when (val content = step.stepContent) {
  is StepContent.AudioRecording -> AudioRecording(
    stepId = step.id,
    text = step.text,
    hint = content.hint,
    uploadUri = content.uploadUri,
  )

  is StepContent.Form -> Form(
    stepId = step.id,
    formFieldList = content.fields.map { field ->
      FormField(
        fieldId = field.id,
        title = field.title,
        type = FormFieldType.TEXT, // todo
        defaultValue = field.defaultValue,
        currentValue = "",
        isRequired = field.isRequired,
        minValue = field.minValue,
        maxValue = field.maxValue,
        options = field.options,
        suffix = field.suffix,
      )
    },
  )

  is StepContent.Summary -> Summary(
    stepId = step.id,
    items = step.stepContent.items,
  )

  is StepContent.Task -> AssistantLoadingState(
    stepId = step.id,
    text = step.text,
    subText = content.description,
    isLoading = !content.isCompleted,
  )

  StepContent.Unknown -> AssistantMessage(
    stepId = step.id,
    text = "I do not know how to respond to that...",
    subText = "(unknown step content)",
  )

  is StepContent.Outcome -> ConversationItem.Outcome(
    text = step.text,
    claimId = content.claimId,
    stepId = step.id,
  )
}
