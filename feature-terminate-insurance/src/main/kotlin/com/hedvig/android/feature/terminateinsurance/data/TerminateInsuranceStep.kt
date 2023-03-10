package com.hedvig.android.feature.terminateinsurance.data

import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import kotlinx.datetime.LocalDate
import octopus.fragment.FlowStepFragment

internal sealed interface TerminateInsuranceStep {
  data class TerminateInsuranceDate(
    val minDate: LocalDate,
    val maxDate: LocalDate,
  ) : TerminateInsuranceStep

  data class TerminateInsuranceSuccess(
    val terminationDate: LocalDate,
    val surveyUrl: String,
  ) : TerminateInsuranceStep

  /**
   * Note that this is not a network error, or trying to show an unknown screen. This is an explicitly returned
   * "Failed" Screen returned from the backend
   */
  data class Failure(val message: String? = "") : TerminateInsuranceStep

  /**
   * When the client does not know how to parse a step, probably due to having an old Schema, it defaults to this
   * screen
   */
  data class UnknownStep(val message: String? = "") : TerminateInsuranceStep
}

internal fun FlowStepFragment.CurrentStep.toTerminateInsuranceStep(): TerminateInsuranceStep {
  return when (this) {
    is FlowStepFragment.FlowTerminationDateStepCurrentStep -> {
      TerminateInsuranceStep.TerminateInsuranceDate(this.minDate, this.maxDate)
    }
    is FlowStepFragment.FlowTerminationFailedStepCurrentStep -> TerminateInsuranceStep.Failure()
    is FlowStepFragment.FlowTerminationSuccessStepCurrentStep -> {
      TerminateInsuranceStep.TerminateInsuranceSuccess(this.terminationDate, this.surveyUrl)
    }
    else -> TerminateInsuranceStep.UnknownStep()
  }
}

internal fun TerminateInsuranceStep.toTerminateInsuranceDestination(): TerminateInsuranceDestination {
  return when (this) {
    is TerminateInsuranceStep.Failure -> TerminateInsuranceDestination.TerminationFailure(message)
    is TerminateInsuranceStep.TerminateInsuranceDate -> {
      TerminateInsuranceDestination.TerminationDate(minDate, maxDate)
    }
    is TerminateInsuranceStep.TerminateInsuranceSuccess -> {
      TerminateInsuranceDestination.TerminationSuccess(terminationDate, surveyUrl)
    }
    is TerminateInsuranceStep.UnknownStep -> TerminateInsuranceDestination.UnknownScreen
  }
}
