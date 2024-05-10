package com.hedvig.android.feature.terminateinsurance.data

import kotlinx.serialization.Serializable

@Serializable
internal data class TerminationSurveyOption(
  val id: String,
  val title: String,
  val feedBackRequired: Boolean,
  val suggestion: SurveyOptionSuggestion?,
  val subOptions: List<TerminationSurveyOption>,
)

@Serializable
internal sealed interface SurveyOptionSuggestion {
  @Serializable
  enum class Action : SurveyOptionSuggestion {
    UPDATE_ADDRESS,
  }

  @Serializable
  data class Redirect(
    val url: String,
    val description: String,
    val buttonTitle: String,
  ) : SurveyOptionSuggestion
}

internal data class TerminationReason(
  val surveyOption: TerminationSurveyOption,
  val feedBack: String?,
)