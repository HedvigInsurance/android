package com.hedvig.android.feature.terminateinsurance.data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal data class TerminationSurveyData(
  val options: List<TerminationSurveyOption>,
  val action: TerminationAction,
)

@Serializable
internal sealed interface TerminationAction {
  val extraCoverageItems: List<ExtraCoverageItem>

  @Serializable
  data class TerminateWithDate(
    val minDate: LocalDate,
    val maxDate: LocalDate,
    override val extraCoverageItems: List<ExtraCoverageItem>,
  ) : TerminationAction

  @Serializable
  data class DeleteInsurance(
    override val extraCoverageItems: List<ExtraCoverageItem>,
  ) : TerminationAction
}

@Serializable
internal data class ExtraCoverageItem(
  val displayName: String,
  val displayValue: String?,
)

internal sealed interface TerminationResult {
  data class Terminated(val terminationDate: LocalDate) : TerminationResult

  data object Deleted : TerminationResult

  data class UserError(val message: String) : TerminationResult
}
