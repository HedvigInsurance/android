package com.hedvig.android.feature.terminateinsurance.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import kotlinx.datetime.LocalDate
import octopus.FlowTerminationDateNextMutation
import octopus.FlowTerminationDeletionNextMutation
import octopus.FlowTerminationStartMutation
import octopus.FlowTerminationSurveyNextMutation
import octopus.type.FlowTerminationDateInput
import octopus.type.FlowTerminationStartInput
import octopus.type.FlowTerminationSurveyDataInput
import octopus.type.FlowTerminationSurveyInput

internal interface TerminateInsuranceRepository {
  suspend fun startTerminationFlow(insuranceId: InsuranceId): Either<ErrorMessage, TerminateInsuranceStep>

  suspend fun setTerminationDate(terminationDate: LocalDate): Either<ErrorMessage, TerminateInsuranceStep>

  suspend fun submitReasonForCancelling(reason: TerminationReason): Either<ErrorMessage, TerminateInsuranceStep>

  suspend fun confirmDeletion(): Either<ErrorMessage, TerminateInsuranceStep>
}

internal class TerminateInsuranceRepositoryImpl(
  private val apolloClient: ApolloClient,
  private val terminationFlowContextStorage: TerminationFlowContextStorage,
) : TerminateInsuranceRepository {
  override suspend fun startTerminationFlow(insuranceId: InsuranceId): Either<ErrorMessage, TerminateInsuranceStep> {
    return either {
      val result = apolloClient
        .mutation(FlowTerminationStartMutation(FlowTerminationStartInput(insuranceId.id)))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowTerminationStart
      terminationFlowContextStorage.saveContext(result.context)
      result.currentStep.toTerminateInsuranceStep()
    }
  }

  override suspend fun setTerminationDate(terminationDate: LocalDate): Either<ErrorMessage, TerminateInsuranceStep> {
    return either {
      val result = apolloClient
        .mutation(
          FlowTerminationDateNextMutation(
            context = terminationFlowContextStorage.getContext(),
            input = FlowTerminationDateInput(terminationDate),
          ),
        )
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowTerminationDateNext
      terminationFlowContextStorage.saveContext(result.context)
      result.currentStep.toTerminateInsuranceStep()
    }
  }

  override suspend fun submitReasonForCancelling(
    reason: TerminationReason,
  ): Either<ErrorMessage, TerminateInsuranceStep> {
    return either {
      val result = apolloClient
        .mutation(
          FlowTerminationSurveyNextMutation(
            context = terminationFlowContextStorage.getContext(),
            input = FlowTerminationSurveyInput(
              data = FlowTerminationSurveyDataInput(
                optionId = reason.surveyOption.id,
                text = Optional.presentIfNotNull(reason.feedBack),
              ),
            ),
          ),
        )
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowTerminationSurveyNext
      terminationFlowContextStorage.saveContext(result.context)
      result.currentStep.toTerminateInsuranceStep()
    }
  }

  override suspend fun confirmDeletion(): Either<ErrorMessage, TerminateInsuranceStep> {
    return either {
      val result = apolloClient
        .mutation(FlowTerminationDeletionNextMutation(terminationFlowContextStorage.getContext()))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowTerminationDeletionNext
      terminationFlowContextStorage.saveContext(result.context)
      result.currentStep.toTerminateInsuranceStep()
    }
  }
}
