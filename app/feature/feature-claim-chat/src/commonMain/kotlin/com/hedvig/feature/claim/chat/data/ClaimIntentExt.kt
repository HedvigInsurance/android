package com.hedvig.feature.claim.chat.data

import octopus.fragment.AudioRecordingFragment
import octopus.fragment.ClaimIntentFragment
import octopus.fragment.ClaimIntentStepContentFragment
import octopus.fragment.FormFragment
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
    is SummaryFragment -> StepContent.Summary(this.audioRecordings.toString() + this.fileUploads + this.items)
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
      optionsTodo = field.options?.joinToString(), // todo
    )
  }
}
