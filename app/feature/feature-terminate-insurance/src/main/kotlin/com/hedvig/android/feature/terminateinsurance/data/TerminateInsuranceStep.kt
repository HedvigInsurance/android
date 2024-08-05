package com.hedvig.android.feature.terminateinsurance.data

import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.Destination
import kotlinx.datetime.LocalDate
import octopus.fragment.FlowTerminationSurveyOptionSuggestionActionFlowTerminationSurveyOptionSuggestionFragment
import octopus.fragment.FlowTerminationSurveyOptionSuggestionFragment
import octopus.fragment.FlowTerminationSurveyOptionSuggestionRedirectFlowTerminationSurveyOptionSuggestionFragment
import octopus.fragment.TerminationFlowStepFragment
import octopus.type.FlowTerminationSurveyRedirectAction

internal sealed interface TerminateInsuranceStep {
  data class TerminateInsuranceDate(
    val minDate: LocalDate,
    val maxDate: LocalDate,
  ) : TerminateInsuranceStep

  data class TerminateInsuranceSuccess(
    val terminationDate: LocalDate?,
  ) : TerminateInsuranceStep

  data object InsuranceDeletion : TerminateInsuranceStep

  data class Survey(
    val options: List<TerminationSurveyOption>,
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
}

internal fun TerminationFlowStepFragment.CurrentStep.toTerminateInsuranceStep(): TerminateInsuranceStep {
  return when (this) {
    is TerminationFlowStepFragment.FlowTerminationDateStepCurrentStep -> {
      TerminateInsuranceStep.TerminateInsuranceDate(minDate, maxDate)
    }

    is TerminationFlowStepFragment.FlowTerminationFailedStepCurrentStep -> TerminateInsuranceStep.Failure()
    is TerminationFlowStepFragment.FlowTerminationDeletionStepCurrentStep -> {
      TerminateInsuranceStep.InsuranceDeletion
    }

    is TerminationFlowStepFragment.FlowTerminationSuccessStepCurrentStep -> {
      TerminateInsuranceStep.TerminateInsuranceSuccess(terminationDate)
    }

    is TerminationFlowStepFragment.FlowTerminationSurveyStepCurrentStep -> {
      TerminateInsuranceStep.Survey(
        options.toOptionList(),
      )
    }

    else -> TerminateInsuranceStep.UnknownStep()
  }
}

private fun List<TerminationFlowStepFragment.FlowTerminationSurveyStepCurrentStep.Option>.toOptionList():
  List<TerminationSurveyOption> {
  return map {
    TerminationSurveyOption(
      id = it.id,
      title = it.title,
      listIndex = this.indexOf(it),
      feedBackRequired = it.feedBack != null,
      subOptions = it.subOptions?.toSubOptionList() ?: listOf(),
      suggestion = it.suggestion?.toSuggestion(),
    )
  }
}

private fun List<TerminationFlowStepFragment.FlowTerminationSurveyStepCurrentStep.Option.SubOption>.toSubOptionList():
  List<TerminationSurveyOption> {
  return map {
    TerminationSurveyOption(
      id = it.id,
      title = it.title,
      feedBackRequired = it.feedBack != null,
      subOptions = listOf(),
      listIndex = this.indexOf(it),
      suggestion = it.suggestion?.toSuggestion(),
    )
  }
}

private fun FlowTerminationSurveyOptionSuggestionFragment.toSuggestion(): SurveyOptionSuggestion? {
  return when (this) {
    is FlowTerminationSurveyOptionSuggestionActionFlowTerminationSurveyOptionSuggestionFragment -> {
      if (action == FlowTerminationSurveyRedirectAction.UPDATE_ADDRESS) {
        SurveyOptionSuggestion.Action.UpdateAddress(
          description = description,
          buttonTitle = buttonTitle,
        )
      } else {
        logcat(
          LogPriority.WARN,
          message = { "FlowTerminationSurveyStepCurrentStep unknown suggestion type: ${this.action.rawValue}" },
        )
        null
      }
    }

    is FlowTerminationSurveyOptionSuggestionRedirectFlowTerminationSurveyOptionSuggestionFragment -> {
      SurveyOptionSuggestion.Redirect(
        buttonTitle = this.buttonTitle,
        description = this.description,
        url = this.url,
      )
    }

    else -> {
      logcat(
        LogPriority.WARN,
        message = { "FlowTerminationSurveyStepCurrentStep unknown suggestion type: $this" },
      )
      null
    }
  }
}

internal fun TerminateInsuranceStep.toTerminateInsuranceDestination(
  commonParams: TerminationGraphParameters,
): Destination {
  return when (this) {
    is TerminateInsuranceStep.Failure -> TerminateInsuranceDestination.TerminationFailure(message)

    is TerminateInsuranceStep.TerminateInsuranceDate -> {
      TerminateInsuranceDestination.TerminationDate(
        minDate = minDate,
        maxDate = maxDate,
        commonParams = commonParams,
      )
    }

    is TerminateInsuranceStep.InsuranceDeletion -> TerminateInsuranceDestination.InsuranceDeletion(
      commonParams = commonParams,
    )

    is TerminateInsuranceStep.TerminateInsuranceSuccess -> TerminateInsuranceDestination.TerminationSuccess(
      terminationDate = terminationDate,
    )

    is TerminateInsuranceStep.UnknownStep -> TerminateInsuranceDestination.UnknownScreen

    is TerminateInsuranceStep.Survey -> TerminateInsuranceDestination.TerminationSurveyFirstStep(
      options = options,
      commonParams = commonParams,
    )
  }
}
