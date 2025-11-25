package com.hedvig.feature.claim.chat.data

import arrow.core.raise.Raise
import com.hedvig.android.core.common.ErrorMessage
import octopus.fragment.AudioRecordingFragment
import octopus.fragment.ClaimIntentFragment
import octopus.fragment.ClaimIntentMutationOutputFragment
import octopus.fragment.ClaimIntentOutcomeClaimFragment
import octopus.fragment.ClaimIntentOutcomeDeflectionFragment
import octopus.fragment.ClaimIntentOutcomeDeflectionInfoBlockFragment
import octopus.fragment.ClaimIntentStepContentFragment
import octopus.fragment.ContentSelectFragment
import octopus.fragment.FileUploadFragment
import octopus.fragment.FormFragment
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
        claim.submittedAt,
      )
    }

    is ClaimIntentOutcomeDeflectionFragment -> {
      ClaimIntentOutcome.Deflect(
        title = title,
        infoText = infoText,
        warningText = warningText,
        partners = partners.map { partner ->
          ClaimIntentOutcome.Deflect.Partner(
            id = partner.id,
            imageUrl = partner.imageUrl,
            phoneNumber = partner.phoneNumber,
            title = partner.title,
            description = partner.description,
            info = partner.info,
            url = partner.url,
            urlButtonTitle = partner.urlButtonTitle,
          )
        },
        partnersInfo = partnersInfo?.toInfoBlock(),
        content = content.toInfoBlock(),
        faq = faq.map { it.toInfoBlock() },
      )
    }

    else -> {
      ClaimIntentOutcome.Unknown
    }
  }
}

private fun ClaimIntentOutcomeDeflectionInfoBlockFragment.toInfoBlock(): ClaimIntentOutcome.Deflect.InfoBlock {
  return ClaimIntentOutcome.Deflect.InfoBlock(
    title,
    description,
  )
}
