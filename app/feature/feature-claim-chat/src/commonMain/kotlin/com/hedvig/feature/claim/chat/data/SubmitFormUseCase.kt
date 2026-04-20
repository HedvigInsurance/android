package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import octopus.ClaimIntentSubmitFormMutation
import octopus.type.ClaimIntentFormSubmitInputField
import octopus.type.ClaimIntentSubmitFormInput

internal class SubmitFormUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun invoke(formData: FormSubmissionData): Either<ClaimChatErrorMessage, ClaimIntent> {
    return either {
      apolloClient
        .mutation(
          ClaimIntentSubmitFormMutation(
            ClaimIntentSubmitFormInput(
              stepId = formData.stepId.value,
              fields = formData.fieldsToSubmit.map { field ->
                ClaimIntentFormSubmitInputField(
                  fieldId = field.fieldId.value,
                  values = field.values.filterNotNull(),
                )
              },
            ),
          ),
        )
        .safeExecute()
        .mapLeft {
          logcat { "SubmitFormUseCase error: $it" }
          ClaimChatErrorMessage.GeneralError
        }
        .bind()
        .claimIntentSubmitForm
        .toClaimIntent(languageService.getLocale())
    }
  }
}

internal data class FormSubmissionData(
  val stepId: StepId,
  val fieldsToSubmit: List<FieldToSubmit>,
) {
  data class FieldToSubmit(
    val fieldId: FieldId,
    val values: List<String?>,
  )
}
