package com.hedvig.feature.claim.chat.data

import arrow.core.raise.Raise
import com.hedvig.android.core.common.ErrorMessage
import octopus.fragment.AudioRecordingFragment
import octopus.fragment.ClaimIntentFragment
import octopus.fragment.ClaimIntentMutationOutputFragment
import octopus.fragment.ClaimIntentOutcomeClaimFragment
import octopus.fragment.ClaimIntentOutcomeDeflectionFragment
import octopus.fragment.ClaimIntentStepContentFragment
import octopus.fragment.ContentSelectFragment
import octopus.fragment.FileUploadFragment
import octopus.fragment.FormFragment
import octopus.fragment.SummaryFragment
import octopus.fragment.TaskFragment
import octopus.type.ClaimIntentOutcomeDeflectionType.EMERGENCY
import octopus.type.ClaimIntentOutcomeDeflectionType.GLASS
import octopus.type.ClaimIntentOutcomeDeflectionType.TOWING
import octopus.type.ClaimIntentOutcomeDeflectionType.EIR
import octopus.type.ClaimIntentOutcomeDeflectionType.PESTS
import octopus.type.ClaimIntentOutcomeDeflectionType.ID_PROTECTION
import octopus.type.ClaimIntentOutcomeDeflectionType.UNKNOWN__


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
    id = ClaimIntentId(id),
    next = when {
      currentStep != null -> ClaimIntent.Next.Step(currentStep!!.toClaimIntentStep())
      outcome != null -> ClaimIntent.Next.Outcome(outcome!!.toClaimIntentOutcome())
      else -> error("ClaimIntentFragment contained null currentStep and null outcome")
    },
    // todo also render source messages
  )
}

private fun ClaimIntentFragment.CurrentStep.toClaimIntentStep(): ClaimIntentStep {
  return ClaimIntentStep(
    id = StepId(id),
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
      fileUploads = fileUploads.map { StepContent.Summary.FileUpload(it.url, it.contentType, it.fileName) },
    )

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
      id = FieldId(field.id),
      isRequired = field.isRequired,
      suffix = field.suffix,
      title = field.title,
      defaultValues = field.defaultValues,
      maxValue = field.maxValue,
      minValue = field.minValue,
      type = field.type.toString(), // todo
      options = field.options?.map { it.title to it.value } ?: emptyList(),
    )
  }
}

private fun ClaimIntentFragment.Outcome.toClaimIntentOutcome(): ClaimIntentOutcome {
  return when (this) {
    is ClaimIntentOutcomeClaimFragment -> {
      ClaimIntentOutcome.Claim(
        claimId,
        claim.submittedAt
      )
    }

    is ClaimIntentOutcomeDeflectionFragment -> {
      ClaimIntentOutcome.Deflect(
        type = when (type) {
          EMERGENCY -> ClaimIntentOutcome.Deflect.Type.EMERGENCY
          GLASS -> ClaimIntentOutcome.Deflect.Type.GLASS
          TOWING -> ClaimIntentOutcome.Deflect.Type.TOWING
          EIR -> ClaimIntentOutcome.Deflect.Type.EIR
          PESTS -> ClaimIntentOutcome.Deflect.Type.PESTS
          ID_PROTECTION -> ClaimIntentOutcome.Deflect.Type.ID_PROTECTION
          UNKNOWN__ -> ClaimIntentOutcome.Deflect.Type.UNKNOWN
          null -> ClaimIntentOutcome.Deflect.Type.UNKNOWN
        },
        title = title,
        description = description,
        partners = partners.joinToString { partner ->
          // todo partners deflect mapping
          """${partner.id}:[${partner.title}]"""
        },
      )
    }
    else -> {
      ClaimIntentOutcome.Unknown
    }
  }
}
