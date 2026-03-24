package com.hedvig.android.feature.terminateinsurance.data

import kotlinx.serialization.Serializable

@Serializable
internal data class TerminationSurveyOption(
  val id: String,
  val listIndex: Int,
  val title: String,
  val feedbackRequired: Boolean,
  val suggestion: SurveyOptionSuggestion?,
  val subOptions: List<TerminationSurveyOption>,
  val isDisabled: Boolean = false,
)

@Serializable
internal data class SurveyOptionSuggestion(
  val type: SuggestionType,
  val description: String,
  val url: String?,
)

@Serializable
internal enum class SuggestionType {
  UPDATE_ADDRESS,
  UPGRADE_COVERAGE,
  DOWNGRADE_PRICE,
  REDIRECT,
  INFO,
  AUTO_CANCEL_SOLD,
  AUTO_CANCEL_SCRAPPED,
  AUTO_DECOMMISSION,
  CAR_DECOMMISSION_INFO,
  CAR_ALREADY_DECOMMISSION,
  UNKNOWN,
}
