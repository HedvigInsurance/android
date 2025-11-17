package com.hedvig.feature.claim.chat.data

import arrow.core.raise.Raise
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.feature.claim.chat.ConversationStep
import octopus.fragment.AudioRecordingFragment
import octopus.fragment.ClaimIntentFragment
import octopus.fragment.ClaimIntentMutationOutputFragment
import octopus.fragment.ClaimIntentStepContentFragment
import octopus.fragment.ContentSelectFragment
import octopus.fragment.FileUploadFragment
import octopus.fragment.FormFragment
import octopus.fragment.OutcomeFragment
import octopus.fragment.SummaryFragment
import octopus.fragment.TaskFragment


context(raise: Raise<ErrorMessage>)
internal fun ClaimIntentMutationOutputFragment.toClaimIntent(): ClaimIntent {
  val userError = userError
  val intent = intent
  return with(raise) {
    when {
      userError != null -> raise(ErrorMessage(userError.message))
      intent != null -> intent.toClaimIntent()
      else -> raise(ErrorMessage("No data"))
    }
  }
}

internal fun ClaimIntentFragment.toClaimIntent(): ClaimIntent {
  return ClaimIntent(
    id = id,
    step = currentStep.toClaimIntentStep(),
    // todo also render source messages
  )
}

private fun ClaimIntentFragment.CurrentStep.toClaimIntentStep(): ClaimIntentStep {
  return ClaimIntentStep(
    id = id,
    text = text,
    stepContent = this.content.toStepContent(),
  )
}

private fun ClaimIntentStepContentFragment.toStepContent(): StepContent {
  return when (this) {
    is FormFragment -> StepContent.Form(this.fields.toFields(), isSkippable, isRegrettable)
    is ContentSelectFragment -> StepContent.ContentSelect(options.toOptions(), isSkippable, isRegrettable)
    is TaskFragment -> StepContent.Task(listOf(description), isCompleted)
    is AudioRecordingFragment -> StepContent.AudioRecording(hint, uploadUri, isSkippable, isRegrettable)
    is FileUploadFragment -> StepContent.FileUpload(uploadUri, isSkippable, isRegrettable)
    is SummaryFragment -> StepContent.Summary(
      items = items.map { StepContent.Summary.Item(it.title, it.value) },
      audioRecordings = audioRecordings.map { StepContent.Summary.AudioRecording(it.url) },
      fileUploads = fileUploads.map { StepContent.Summary.FileUpload(it.url, it.contentType, it.fileName) }
    )
    is OutcomeFragment -> StepContent.Outcome(claimId)
    else -> StepContent.Unknown
  }
}

private fun List<ContentSelectFragment.Option>.toOptions(): List<StepContent.ContentSelect.Option> {
  return map { option ->
    StepContent.ContentSelect.Option(
      option.id,
      option.title,
    )
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
      options = field.options?.map { it.title to it.value } ?: emptyList(),
    )
  }
}

internal fun ClaimIntent.createConversationItem(): ConversationStep = TODO()
//when (val content = step.stepContent) {
//  is StepContent.AudioRecording -> AudioRecording(
//    stepId = step.id,
//    text = step.text,
//    hint = content.hint,
//    uploadUri = content.uploadUri,
//  )
//
//  is StepContent.Form -> Form(
//    stepId = step.id,
//    formFieldList = content.fields.map { field ->
//      FormField(
//        fieldId = field.id,
//        title = field.title,
//        type = FormFieldType.TEXT, // todo
//        defaultValue = field.defaultValue,
//        currentValue = "",
//        isRequired = field.isRequired,
//        minValue = field.minValue,
//        maxValue = field.maxValue,
//        options = field.options,
//        suffix = field.suffix,
//      )
//    },
//  )
//
//  is StepContent.Summary -> Summary(
//    stepId = step.id,
//    items = step.stepContent.items,
//  )
//
//  is StepContent.Task -> AssistantLoadingState(
//    stepId = step.id,
//    text = step.text,
//    subText = content.description,
//    isLoading = !content.isCompleted,
//  )
//
//  StepContent.Unknown -> AssistantMessage(
//    stepId = step.id,
//    text = "I do not know how to respond to that...",
//    subText = "(unknown step content)",
//  )
//
//  is StepContent.Outcome -> ConversationItem.Outcome(
//    text = step.text,
//    claimId = content.claimId,
//    stepId = step.id,
//  )
//}
