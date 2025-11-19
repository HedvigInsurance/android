package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.ClaimIntentSubmitFormMutation
import octopus.type.ClaimIntentFormSubmitInputField
import octopus.type.ClaimIntentSubmitFormInput

internal class SubmitFormUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(
    formData: FormSubmissionData,
  ): Either<ErrorMessage, ClaimIntent> {
    return either {
      apolloClient
        .mutation(
          ClaimIntentSubmitFormMutation(
            ClaimIntentSubmitFormInput(
              stepId = formData.stepId.value,
              fields = formData.fields.map { field ->
                ClaimIntentFormSubmitInputField(
                  fieldId = field.fieldId.value,
                  values = field.values.filterNotNull(),
                )
              },
            ),
          ),
        )
        .safeExecute()
        .mapLeft(::ErrorMessage)
        .bind()
        .claimIntentSubmitForm
        .toClaimIntent()
    }
  }
}

internal data class FormSubmissionData(
  val stepId: StepId,
  val fields: List<Field>,
) {
  data class Field(
    val fieldId: FieldId,
    val values: List<String?>,
  )
}
