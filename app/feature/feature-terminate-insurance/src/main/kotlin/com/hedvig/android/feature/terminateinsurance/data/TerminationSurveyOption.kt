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
  val redirection: SurveyOptionRedirection? = null,
  val isDisabled: Boolean = false,
)

@Serializable
internal data class SurveyOptionSuggestion(
  val type: SuggestionType,
  val description: String,
  /**
   * The label for the suggestion's action button, when the backend has one to offer. Suggestions without their own
   * label fall back to a label picked from the [type].
   */
  val actionText: String? = null,
  val url: String?,
)

@Serializable
internal data class SurveyOptionRedirection(
  val title: String,
  val description: String,
  val type: RedirectionType,
  val actionText: String,
  val image: RedirectionImage?,
)

@Serializable
internal data class RedirectionImage(
  val url: String,
  val overlayText: String?,
)

@Serializable
internal enum class RedirectionType {
  UPDATE_ADDRESS,
  UNKNOWN,
}

@Serializable
internal enum class SuggestionType {
  UPDATE_ADDRESS,
  UPGRADE_COVERAGE,
  DOWNGRADE_PRICE,
  REDIRECT,
  INFO,
  AUTO_CANCEL_SOLD,
  AUTO_CANCEL_SCRAPPED,
  AUTO_CANCEL_DECOMMISSION,
  AUTO_DECOMMISSION,
  CAR_ALREADY_DECOMMISSION,
  UNKNOWN,
  ;

  companion object {
    val DEFLECT_TYPES = setOf(
      AUTO_DECOMMISSION,
      AUTO_CANCEL_SOLD,
      AUTO_CANCEL_SCRAPPED,
      AUTO_CANCEL_DECOMMISSION,
      CAR_ALREADY_DECOMMISSION,
    )
  }
}
