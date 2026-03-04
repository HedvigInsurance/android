package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import octopus.FormFieldSearchQuery
import octopus.type.ClaimIntentFormFieldSearchInput

internal interface FormFieldSearchUseCase {
  suspend fun invoke(
    stepId: String,
    fieldId: String,
    query: String,
  ): Either<ErrorMessage, List<StepContent.Form.FieldOption>>
}


internal class FormFieldSearchUseCaseImpl(
  private val apolloClient: ApolloClient,
) : FormFieldSearchUseCase {
  override suspend fun invoke(
    stepId: String,
    fieldId: String,
    query: String,
  ): Either<ErrorMessage, List<StepContent.Form.FieldOption>> {
    return either {
      val data = apolloClient
        .query(
          FormFieldSearchQuery(
            ClaimIntentFormFieldSearchInput(
              stepId = stepId,
              fieldId = fieldId,
              query = query,
            ),
          ),
        )
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .mapLeft {
          logcat { "FormFieldSearchUseCase error: $it" }
          ErrorMessage()
        }
        .bind()
        .claimIntentFormFieldSearch.options
      data.map { option ->
        StepContent.Form.FieldOption(
          value = option.value,
          text = option.title,
          subtitle = option.subtitle,
          imageUrl = option.imageUrl,
        )
      }
    }
  }
}
