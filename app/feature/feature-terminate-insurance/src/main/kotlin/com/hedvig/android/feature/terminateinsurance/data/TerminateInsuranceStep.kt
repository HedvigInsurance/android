package com.hedvig.android.feature.terminateinsurance.data

import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion.Action.UnknownAction
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.common.Destination
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.fragment.ExtraCoverageItemFragment
import octopus.fragment.FlowTerminationSurveyOptionSuggestionActionFlowTerminationSurveyOptionSuggestionFragment
import octopus.fragment.FlowTerminationSurveyOptionSuggestionFragment
import octopus.fragment.FlowTerminationSurveyOptionSuggestionRedirectFlowTerminationSurveyOptionSuggestionFragment
import octopus.fragment.TerminationFlowStepFragment
import octopus.type.FlowTerminationSurveyRedirectAction

internal sealed interface TerminateInsuranceStep {
  data class TerminateInsuranceDate(
    val minDate: LocalDate,
    val maxDate: LocalDate,
    val extraCoverageItems: List<ExtraCoverageItem>,
  ) : TerminateInsuranceStep

  data class TerminateInsuranceSuccess(
    val terminationDate: LocalDate?,
  ) : TerminateInsuranceStep

  data class InsuranceDeletion(
    val extraCoverageItems: List<ExtraCoverageItem>,
  ) : TerminateInsuranceStep

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
      TerminateInsuranceStep.TerminateInsuranceDate(minDate, maxDate, extraCoverage.toExtraCoverageItems())
    }

    is TerminationFlowStepFragment.FlowTerminationFailedStepCurrentStep -> TerminateInsuranceStep.Failure()
    is TerminationFlowStepFragment.FlowTerminationDeletionStepCurrentStep -> {
      TerminateInsuranceStep.InsuranceDeletion(extraCoverage.toExtraCoverageItems())
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
    // remade a bit of logic here. If we receive unknown actions in suggestion for one of the subOptions
    // (or the option itself),
    // subOptions then become empty and instead the freeTextField becomes available
    TerminationSurveyOption(
      id = it.id,
      title = it.title,
      listIndex = this.indexOf(it),
      feedBackRequired = it.feedBack != null ||
        (it.suggestion?.toSuggestion() == UnknownAction) ||
        it.subOptions?.noUnknownActions() == false,
      subOptions = it.subOptions?.toSubOptionList() ?: emptyList(),
      suggestion = it.suggestion?.toSuggestion(),
    )
  }
}

private fun List<TerminationFlowStepFragment.FlowTerminationSurveyStepCurrentStep.Option.SubOption>.noUnknownActions():
  Boolean {
  return none { subOption ->
    subOption.suggestion?.toSuggestion() == UnknownAction
  }
}

private fun List<TerminationFlowStepFragment.FlowTerminationSurveyStepCurrentStep.Option.SubOption>.toSubOptionList():
  List<TerminationSurveyOption> {
  // no subOptions if one of them contains some action that we don't know how to handle
  val filtered = takeIf { subs ->
    subs.noUnknownActions()
  } ?: listOf()
  return filtered.map { subOption ->
    TerminationSurveyOption(
      id = subOption.id,
      title = subOption.title,
      feedBackRequired = subOption.feedBack != null,
      subOptions = listOf(),
      listIndex = filtered.indexOf(subOption),
      suggestion = subOption.suggestion?.toSuggestion(),
    )
  }
}

private fun FlowTerminationSurveyOptionSuggestionFragment.toSuggestion(): SurveyOptionSuggestion? {
  val isTierFeatureEnabled = true
  return when (this) {
    is FlowTerminationSurveyOptionSuggestionActionFlowTerminationSurveyOptionSuggestionFragment -> {
      when (action) {
        FlowTerminationSurveyRedirectAction.UPDATE_ADDRESS -> {
          SurveyOptionSuggestion.Action.UpdateAddress(
            description = description,
            buttonTitle = buttonTitle,
          )
        }

        FlowTerminationSurveyRedirectAction.CHANGE_TIER_FOUND_BETTER_PRICE -> {
          SurveyOptionSuggestion.Action.DowngradePriceByChangingTier(
            description = description,
            buttonTitle = buttonTitle,
          )
        }

        FlowTerminationSurveyRedirectAction.CHANGE_TIER_MISSING_COVERAGE_AND_TERMS -> {
          if (isTierFeatureEnabled) {
            SurveyOptionSuggestion.Action.UpgradeCoverageByChangingTier(
              description = description,
              buttonTitle = buttonTitle,
            )
          } else {
            logcat(
              LogPriority.ERROR,
              message = {
                "FlowTerminationSurveyStepCurrentStep suggestion: CHANGE_TIER_MISSING_COVERAGE_AND_TERMS but tier feature flag is disabled!"
              },
            )
            UnknownAction
          }
        }

        else -> {
          logcat(
            LogPriority.WARN,
            message = { "FlowTerminationSurveyStepCurrentStep unknown suggestion type: ${this.action.rawValue}" },
          )
          UnknownAction
        }
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
        extraCoverageItems = extraCoverageItems,
        commonParams = commonParams,
      )
    }

    is TerminateInsuranceStep.InsuranceDeletion -> TerminateInsuranceDestination.InsuranceDeletion(
      commonParams = commonParams,
      extraCoverageItems = extraCoverageItems,
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

private fun List<ExtraCoverageItemFragment>?.toExtraCoverageItems(): List<ExtraCoverageItem> {
  return this?.map {
    ExtraCoverageItem(it.displayName, it.displayValue)
  } ?: emptyList()
}

@Serializable
internal data class ExtraCoverageItem(
  val displayName: String,
  val displayValue: String?,
)
