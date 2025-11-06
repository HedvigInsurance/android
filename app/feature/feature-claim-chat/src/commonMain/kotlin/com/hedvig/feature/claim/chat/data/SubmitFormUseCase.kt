package com.hedvig.feature.claim.chat.data

import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.feature.claim.chat.data.StepContent.Form.Field
import octopus.ClaimIntentSubmitFormMutation
import octopus.type.ClaimIntentFormSubmitInputField
import octopus.type.ClaimIntentSubmitFormInput

internal class SubmitFormUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(
    stepId: String,
    fields: List<Field>,
  ) = either {
    val data = apolloClient
      .mutation(
        ClaimIntentSubmitFormMutation(
          ClaimIntentSubmitFormInput(
            stepId = stepId,
            fields = fields.map {
              ClaimIntentFormSubmitInputField(it.id) // todo
            },
          ),
        ),
      )
      .safeExecute()
      .mapLeft(::ErrorMessage)
      .bind()
      .claimIntentSubmitForm

    when {
      data.userError != null -> raise(ErrorMessage(data.userError.message))
      data.intent != null -> data.intent.currentStep.toClaimIntentStep()
      else -> raise(ErrorMessage("No data"))
    }
  }
}
