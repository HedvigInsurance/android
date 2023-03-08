package com.hedvig.android.feature.terminateinsurance.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import kotlinx.datetime.toKotlinLocalDate
import octopus.FlowTerminationDateNextMutation
import octopus.FlowTerminationStartMutation
import octopus.type.FlowTerminationDateInput
import java.time.LocalDate

internal class TerminateInsuranceRepository(
  private val apolloClient: ApolloClient,
) {
  private var terminationContext: Any? = null

  suspend fun startTerminationFlow(insuranceId: InsuranceId): TerminationStep {
    return when (val res = apolloClient.mutation(FlowTerminationStartMutation(insuranceId.id)).safeExecute()) {
      is OperationResult.Error -> TerminationStep.Failed(res.message)
      is OperationResult.Success -> {
        terminationContext = res.data.flowTerminationStart.context
        res.data.flowTerminationStart.currentStep.toTerminationStep()
      }
    }
  }

  suspend fun setTerminationDate(terminationDate: LocalDate): TerminationStep {
    val nextMutation = FlowTerminationDateNextMutation(
      context = terminationContext!!,
      input = FlowTerminationDateInput(terminationDate.toKotlinLocalDate()),
    )
    return when (val res = apolloClient.mutation(nextMutation).safeExecute()) {
      is OperationResult.Error -> TerminationStep.Failed(res.message)
      is OperationResult.Success -> {
        terminationContext = res.data.flowTerminationDateNext.context
        res.data.flowTerminationDateNext.currentStep.toTerminationStep()
      }
    }
  }
}

private fun FlowTerminationStartMutation.Data.FlowTerminationStart.CurrentStep.toTerminationStep(): TerminationStep = when (this) {
  is FlowTerminationStartMutation.Data.FlowTerminationStart.FlowTerminationDateStepCurrentStep -> TerminationStep.Date(minDate, maxDate)
  is FlowTerminationStartMutation.Data.FlowTerminationStart.FlowTerminationFailedStepCurrentStep -> TerminationStep.Failed("error")
  is FlowTerminationStartMutation.Data.FlowTerminationStart.FlowTerminationSuccessStepCurrentStep -> TerminationStep.Success(terminationDate, surveyUrl)
  is FlowTerminationStartMutation.Data.FlowTerminationStart.OtherCurrentStep -> TerminationStep.Failed("Unknown step: OtherCurrentStep")
}

private fun FlowTerminationDateNextMutation.Data.FlowTerminationDateNext.CurrentStep.toTerminationStep(): TerminationStep = when (this) {
  is FlowTerminationDateNextMutation.Data.FlowTerminationDateNext.FlowTerminationDateStepCurrentStep -> TerminationStep.Date(minDate, maxDate)
  is FlowTerminationDateNextMutation.Data.FlowTerminationDateNext.FlowTerminationFailedStepCurrentStep -> TerminationStep.Failed("error")
  is FlowTerminationDateNextMutation.Data.FlowTerminationDateNext.FlowTerminationSuccessStepCurrentStep -> TerminationStep.Success(terminationDate, surveyUrl)
  is FlowTerminationDateNextMutation.Data.FlowTerminationDateNext.OtherCurrentStep -> TerminationStep.Failed("Unknown step: OtherCurrentStep")
}

