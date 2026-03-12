package com.hedvig.feature.claim.chat.data

import arrow.core.raise.Raise
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.locale.CommonLocale
import com.hedvig.android.design.system.hedvig.DatePickerUiState
import com.hedvig.android.logger.logcat
import kotlinx.datetime.LocalDate
import octopus.fragment.AudioRecordingFragment
import octopus.fragment.ClaimIntentFragment
import octopus.fragment.ClaimIntentMutationOutputFragment
import octopus.fragment.ClaimIntentStepContentFragment
import octopus.fragment.ContentSelectFragment
import octopus.fragment.DeflectionFragment
import octopus.fragment.DeflectionInfoBlockFragment
import octopus.fragment.FileUploadFragment
import octopus.fragment.FormFragment
import octopus.fragment.SummaryFragment
import octopus.fragment.TaskFragment
import octopus.type.ClaimIntentStepContentFormFieldType
import octopus.type.ClaimIntentStepContentSelectStyle

context(raise: Raise<ErrorMessage>)
internal fun ClaimIntentMutationOutputFragment.toClaimIntent(locale: CommonLocale): ClaimIntent {
  val userError = userError
  val intent = intent
  return with(raise) {
    when {
      userError != null -> raise(ErrorMessage(userError.message))
      intent != null -> intent.toClaimIntent(locale)
      else -> raise(ErrorMessage("No data"))
    }
  }
}

internal fun ClaimIntentFragment.toClaimIntent(locale: CommonLocale): ClaimIntent {
  return ClaimIntent(
    id = ClaimIntentId(id),
    next = when {
      currentStep != null -> ClaimIntent.Next.Step(currentStep!!.toClaimIntentStep(locale))
      createdClaim != null -> ClaimIntent.Next.Outcome(createdClaim!!.toClaimIntentOutcome())
      else -> error("ClaimIntentFragment contained null currentStep and null outcome")
    },
    progress = progress?.toFloat(),
  )
}

private fun ClaimIntentFragment.CurrentStep.toClaimIntentStep(locale: CommonLocale): ClaimIntentStep {
  return ClaimIntentStep(
    id = StepId(id),
    text = text,
    stepContent = this.content.toStepContent(locale),
    isRegrettable = this.isRegrettable,
    hint = hint,
  )
}

private fun ClaimIntentStepContentFragment.toStepContent(locale: CommonLocale): StepContent {
  return when (this) {
    is FormFragment -> {
      StepContent.Form(
        fields = this.fields.toFields(locale),
        isSkippable = isSkippable,
      )
    }

    is ContentSelectFragment -> {
      StepContent.ContentSelect(
        options = options.toOptions(),
        selectedOptionId = defaultSelectedId,
        isSkippable = isSkippable,
        style = when (style) {
          ClaimIntentStepContentSelectStyle.PILL -> StepContent.ContentSelectStyle.PILL
          ClaimIntentStepContentSelectStyle.BINARY -> StepContent.ContentSelectStyle.BINARY
          ClaimIntentStepContentSelectStyle.UNKNOWN__ -> StepContent.ContentSelectStyle.PILL
        },
      )
    }

    is TaskFragment -> {
      StepContent.Task(
        descriptions = listOf(element = description),
        isCompleted = isCompleted,
        failedToSubmit = false,
      )
    }

    is AudioRecordingFragment -> {
      StepContent.AudioRecording(
        uploadUri = uploadUri,
        isSkippable = isSkippable,
        recordingState = AudioRecordingStepState.AudioRecording.NotRecording,
        freeTextMinLength = freeTextMinLength,
        freeTextMaxLength = freeTextMaxLength,
      )
    }

    is FileUploadFragment -> {
      StepContent.FileUpload(
        uploadUri = uploadUri,
        isSkippable = isSkippable,
        localFiles = emptyList(),
      )
    }

    is SummaryFragment -> {
      StepContent.Summary(
        items = items.map { StepContent.Summary.Item(it.title, it.value) },
        audioRecordings = audioRecordings.map { StepContent.Summary.AudioRecording(it.url) },
        fileUploads = fileUploads.map {
          StepContent.Summary.FileUpload(
            it.url,
            it.contentType,
            it.fileName,
          )
        },
        freeTexts = freeTexts,
      )
    }

    is DeflectionFragment -> {
      fun DeflectionInfoBlockFragment.toInfoBlock(): StepContent.Deflect.InfoBlock {
        return StepContent.Deflect.InfoBlock(title, description)
      }
      val partners = if (partners.isNotEmpty()) {
        StepContent.Deflect.DeflectPartnerContainer.ExtendedPartnerContainer(
          partners = partners.map { partner ->
            StepContent.Deflect.DeflectPartnerContainer.ExtendedPartner(
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
        )
      } else if (simplePartners.isNotEmpty()) {
        StepContent.Deflect.DeflectPartnerContainer.SimplePartnerContainer(
          partners = simplePartners.map { partner ->
            StepContent.Deflect.DeflectPartnerContainer.SimplePartner(
              url = partner.url,
              urlButtonTitle = partner.urlButtonTitle,
            )
          },
        )
      } else {
        logcat { "DeflectionFragment: both partners and simplePartners came empty" }
        null
      }

      StepContent.Deflect(
        title = title,
        infoText = infoText,
        warningText = warningText,
        partnersContainer = partners,
        partnersInfo = partnersInfo?.toInfoBlock(),
        content = content.toInfoBlock(),
        faq = faq.map { it.toInfoBlock() },
        buttonText = buttonTitle,
      )
    }

    else -> {
      StepContent.Unknown
    }
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

private fun List<FormFragment.Field>.toFields(locale: CommonLocale): List<StepContent.Form.Field> {
  return this.map { field ->
    StepContent.Form.Field(
      id = FieldId(field.id),
      isRequired = field.isRequired,
      suffix = field.suffix,
      title = field.title,
      defaultValues = field.defaultValues.toFieldOptions(field.options),
      maxValue = field.maxValue,
      minValue = field.minValue,
      type = when (field.type) {
        ClaimIntentStepContentFormFieldType.TEXT -> StepContent.Form.FieldType.TEXT
        ClaimIntentStepContentFormFieldType.DATE -> StepContent.Form.FieldType.DATE
        ClaimIntentStepContentFormFieldType.NUMBER -> StepContent.Form.FieldType.NUMBER
        ClaimIntentStepContentFormFieldType.SINGLE_SELECT -> StepContent.Form.FieldType.SINGLE_SELECT
        ClaimIntentStepContentFormFieldType.MULTI_SELECT -> StepContent.Form.FieldType.MULTI_SELECT
        ClaimIntentStepContentFormFieldType.BINARY -> StepContent.Form.FieldType.BINARY
        ClaimIntentStepContentFormFieldType.PHONE_NUMBER -> StepContent.Form.FieldType.NUMBER
        ClaimIntentStepContentFormFieldType.SEARCH -> null
        ClaimIntentStepContentFormFieldType.UNKNOWN__ -> null
      },
      options = field.options?.map {
        StepContent.Form.FieldOption(
          text = it.title,
          value = it.value,
          subtitle = it.subtitle,
        )
      } ?: emptyList(),
      selectedOptions = field.defaultValues.toFieldOptions(field.options),
      datePickerUiState = when (field.type) {
        ClaimIntentStepContentFormFieldType.DATE -> {
          DatePickerUiState(
            locale = locale,
            initiallySelectedDate = field.defaultValues.getOrNull(0)?.let { LocalDate.parse(it) },
            minDate = field.minValue?.let { LocalDate.parse(it) } ?: LocalDate(1900, 1, 1),
            maxDate = field.maxValue?.let { LocalDate.parse(it) } ?: LocalDate(2100, 1, 1),
          )
        }

        else -> {
          null
        }
      },
    )
  }
}

private fun List<String>.toFieldOptions(
  allOptions: List<FormFragment.Field.Option>?,
): List<StepContent.Form.FieldOption> {
  return this.map { defaultStringValue ->
    allOptions?.firstOrNull { it.value == defaultStringValue }
      ?.let {
        StepContent.Form.FieldOption(
          value = it.value,
          text = it.title, // if we have a list to choose from
          subtitle = it.subtitle,
        )
      }
      ?: // if it is just a value
      StepContent.Form.FieldOption(defaultStringValue, defaultStringValue, null)
  }
}

private fun ClaimIntentFragment.CreatedClaim.toClaimIntentOutcome(): ClaimIntentOutcome {
  return ClaimIntentOutcome.Claim(
    id,
    submittedAt,
  )
}
