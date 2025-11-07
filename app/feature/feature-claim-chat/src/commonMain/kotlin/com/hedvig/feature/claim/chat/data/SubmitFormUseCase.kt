package com.hedvig.feature.claim.chat.data

import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.feature.claim.chat.FormField
import octopus.ClaimIntentSubmitFormMutation
import octopus.type.ClaimIntentFormSubmitInputField
import octopus.type.ClaimIntentSubmitFormInput

internal class SubmitFormUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(
    stepId: String,
    fields: List<FormField>,
  ) = either {
    val data = apolloClient
      .mutation(
        ClaimIntentSubmitFormMutation(
          ClaimIntentSubmitFormInput(
            stepId = stepId,
            fields = fields.map {
              val list = Optional.presentIfNotNull(listOf(it.defaultValue ?: it.options.firstOrNull()?.second ?: ""))
              ClaimIntentFormSubmitInputField(it.fieldId, list) // todo
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
      data.intent != null -> ClaimIntent(data.intent.id, data.intent.currentStep.toClaimIntentStep())
      else -> raise(ErrorMessage("No data"))
    }
  }
}
