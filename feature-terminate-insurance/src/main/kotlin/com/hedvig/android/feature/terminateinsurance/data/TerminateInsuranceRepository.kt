package com.hedvig.android.feature.terminateinsurance.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import octopus.FlowTerminationDateNextMutation
import octopus.FlowTerminationDateNextMutation.Data.FlowTerminationDateNext
import octopus.FlowTerminationStartMutation
import octopus.FlowTerminationStartMutation.Data.FlowTerminationStart
import octopus.type.FlowTerminationDateInput

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
      input = FlowTerminationDateInput(terminationDate),
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

private fun FlowTerminationStart.CurrentStep.toTerminationStep(): TerminationStep = when (this) {
  is FlowTerminationStart.FlowTerminationDateStepCurrentStep -> TerminationStep.Date(minDate, maxDate)
  is FlowTerminationStart.FlowTerminationFailedStepCurrentStep -> TerminationStep.Failed("error")
  is FlowTerminationStart.FlowTerminationSuccessStepCurrentStep -> TerminationStep.Success(terminationDate, surveyUrl)
  is FlowTerminationStart.OtherCurrentStep -> TerminationStep.Failed("Unknown step: OtherCurrentStep")
}

private fun FlowTerminationDateNext.CurrentStep.toTerminationStep(): TerminationStep = when (this) {
  is FlowTerminationDateNext.FlowTerminationDateStepCurrentStep -> TerminationStep.Date(minDate, maxDate)
  is FlowTerminationDateNext.FlowTerminationFailedStepCurrentStep -> TerminationStep.Failed("error")
  is FlowTerminationDateNext.FlowTerminationSuccessStepCurrentStep -> TerminationStep.Success(terminationDate, surveyUrl)
  is FlowTerminationDateNext.OtherCurrentStep -> TerminationStep.Failed("Unknown step: OtherCurrentStep")
}

