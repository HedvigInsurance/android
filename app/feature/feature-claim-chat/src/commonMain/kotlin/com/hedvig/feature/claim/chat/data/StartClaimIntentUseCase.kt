package com.hedvig.feature.claim.chat.data

import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import octopus.ClaimIntentStartMutation
import octopus.fragment.AudioRecordingFragment
import octopus.fragment.ClaimIntentFragment
import octopus.fragment.ClaimIntentStepContentFragment
import octopus.fragment.FormFragment
import octopus.fragment.SummaryFragment
import octopus.fragment.TaskFragment

internal class StartClaimIntentUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(sourceMessageId: String?) {
    either {
      val data = apolloClient
        .mutation(ClaimIntentStartMutation(Optional.presentIfNotNull(sourceMessageId)))
        .safeExecute()
        .mapLeft(::ErrorMessage)
        .bind()
        .claimIntentStart
      ClaimIntent(
        id = data.id,
        step = data.currentStep.toClaimIntentStep(),
      )
    }
  }

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
