package com.hedvig.android.feature.terminateinsurance.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature.TRAVEL_ADDON
import kotlinx.coroutines.flow.first
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

  suspend fun submitReasonForCancelling(
    reason: TerminationSurveyOption,
    feedback: String?,
  ): Either<ErrorMessage, TerminateInsuranceStep>

  suspend fun confirmDeletion(): Either<ErrorMessage, TerminateInsuranceStep>

  suspend fun getContractId(): String
}

internal class TerminateInsuranceRepositoryImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
  private val terminationFlowContextStorage: TerminationFlowContextStorage,
) : TerminateInsuranceRepository {
  override suspend fun startTerminationFlow(insuranceId: InsuranceId): Either<ErrorMessage, TerminateInsuranceStep> {
    return either {
      val isAddonsEnabled = featureManager.isFeatureEnabled(TRAVEL_ADDON).first()
      val result = apolloClient
        .mutation(FlowTerminationStartMutation(FlowTerminationStartInput(insuranceId.id), isAddonsEnabled))
        .safeExecute(::ErrorMessage)
        .bind()
        .flowTerminationStart
      terminationFlowContextStorage.saveContext(result.context)
      terminationFlowContextStorage.saveContractId(insuranceId.id)
      result.currentStep.toTerminateInsuranceStep()
    }
  }

  override suspend fun setTerminationDate(terminationDate: LocalDate): Either<ErrorMessage, TerminateInsuranceStep> {
    return either {
      val isAddonsEnabled = featureManager.isFeatureEnabled(TRAVEL_ADDON).first()
      val result = apolloClient
        .mutation(
          FlowTerminationDateNextMutation(
            context = terminationFlowContextStorage.getContext(),
            input = FlowTerminationDateInput(terminationDate),
            addonsEnabled = isAddonsEnabled,
          ),
        )
        .safeExecute(::ErrorMessage)
        .bind()
        .flowTerminationDateNext
      terminationFlowContextStorage.saveContext(result.context)
      result.currentStep.toTerminateInsuranceStep()
    }
  }

  override suspend fun submitReasonForCancelling(
    reason: TerminationSurveyOption,
    feedback: String?,
  ): Either<ErrorMessage, TerminateInsuranceStep> {
    return either {
      val isAddonsEnabled = featureManager.isFeatureEnabled(TRAVEL_ADDON).first()
      val result = apolloClient
        .mutation(
          FlowTerminationSurveyNextMutation(
            context = terminationFlowContextStorage.getContext(),
            input = FlowTerminationSurveyInput(
              data = FlowTerminationSurveyDataInput(
                optionId = reason.id,
                text = Optional.presentIfNotNull(feedback),
              ),
            ),
            addonsEnabled = isAddonsEnabled,
          ),
        )
        .safeExecute(::ErrorMessage)
        .bind()
        .flowTerminationSurveyNext
      terminationFlowContextStorage.saveContext(result.context)
      result.currentStep.toTerminateInsuranceStep()
    }
  }

  override suspend fun confirmDeletion(): Either<ErrorMessage, TerminateInsuranceStep> {
    return either {
      val isAddonsEnabled = featureManager.isFeatureEnabled(TRAVEL_ADDON).first()
      val result = apolloClient
        .mutation(FlowTerminationDeletionNextMutation(terminationFlowContextStorage.getContext(), isAddonsEnabled))
        .safeExecute(::ErrorMessage)
        .bind()
        .flowTerminationDeletionNext
      terminationFlowContextStorage.saveContext(result.context)
      result.currentStep.toTerminateInsuranceStep()
    }
  }

  override suspend fun getContractId(): String {
    return terminationFlowContextStorage.getContractId()
  }
}
