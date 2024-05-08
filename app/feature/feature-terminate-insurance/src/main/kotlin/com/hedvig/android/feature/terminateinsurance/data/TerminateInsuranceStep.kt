package com.hedvig.android.feature.terminateinsurance.data

import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import kotlinx.datetime.LocalDate
import octopus.fragment.TerminationFlowStepFragment

internal sealed interface TerminateInsuranceStep {
  data class TerminateInsuranceDate(
    val minDate: LocalDate,
    val maxDate: LocalDate,
  ) : TerminateInsuranceStep

  data class TerminateInsuranceSuccess(
    val terminationDate: LocalDate?,
    val surveyUrl: String,
  ) : TerminateInsuranceStep

  data class InsuranceDeletion(
    val disclaimer: String,
  ) : TerminateInsuranceStep

  /**
   * Note that this is not a network error, or trying to show an unknown screen. This is an explicitly returned
   * "Failed" Screen returned from the backend
   */
  data class Failure(val message: String? = null) : TerminateInsuranceStep

  /**
   * When the client does not know how to parse a step, probably due to having an old Schema, it defaults to this
   * screen
   */
  data class UnknownStep(val message: String? = "") : TerminateInsuranceStep

  // todo: add TerminateInsuranceStep.Survey(val options: List<TerminationSurveyOption>)
}

internal fun TerminationFlowStepFragment.CurrentStep.toTerminateInsuranceStep(): TerminateInsuranceStep {
  return when (this) {
    is TerminationFlowStepFragment.FlowTerminationDateStepCurrentStep -> {
      TerminateInsuranceStep.TerminateInsuranceDate(minDate, maxDate)
    }

    is TerminationFlowStepFragment.FlowTerminationFailedStepCurrentStep -> TerminateInsuranceStep.Failure()
    is TerminationFlowStepFragment.FlowTerminationDeletionStepCurrentStep -> {
      TerminateInsuranceStep.InsuranceDeletion(disclaimer)
    }

    is TerminationFlowStepFragment.FlowTerminationSuccessStepCurrentStep -> {
      TerminateInsuranceStep.TerminateInsuranceSuccess(terminationDate, surveyUrl)
    }

    else -> TerminateInsuranceStep.UnknownStep()
  }
}

internal fun TerminateInsuranceStep.toTerminateInsuranceDestination(
  insuranceDisplayName: String,
  exposureName: String,
  activeFrom: LocalDate,
  contractGroup: ContractGroup,
): TerminateInsuranceDestination {
  return when (this) {
    is TerminateInsuranceStep.Failure -> TerminateInsuranceDestination.TerminationFailure(message)
    is TerminateInsuranceStep.TerminateInsuranceDate -> {
      TerminateInsuranceDestination.TerminationDate(
        minDate = minDate,
        maxDate = maxDate,
        insuranceDisplayName = insuranceDisplayName,
        exposureName = exposureName,
        activeFrom = activeFrom,
        contractGroup = contractGroup,
      )
    }

    is TerminateInsuranceStep.InsuranceDeletion -> {
      TerminateInsuranceDestination.InsuranceDeletion(
        insuranceDisplayName = insuranceDisplayName,
        exposureName = exposureName,
        activeFrom = activeFrom,
        contractGroup = contractGroup,
      )
    }

    is TerminateInsuranceStep.TerminateInsuranceSuccess -> {
      TerminateInsuranceDestination.TerminationSuccess(
        insuranceDisplayName = insuranceDisplayName,
        exposureName = exposureName,
        terminationDate = terminationDate,
        surveyUrl = surveyUrl,
      )
    }

    is TerminateInsuranceStep.UnknownStep -> TerminateInsuranceDestination.UnknownScreen

    // todo: add TerminateInsuranceStep.Survey
  }
}
