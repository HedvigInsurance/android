package com.hedvig.android.feature.terminateinsurance.data

import kotlinx.serialization.Serializable

@Serializable
internal data class TerminationSurveyOption(
  val id: String,
  val title: String,
  val feedBackRequired: Boolean,
  val suggestion: SurveyOptionSuggestion,
  val subOptions: List<TerminationSurveyOption>
)

@Serializable
internal sealed interface SurveyOptionSuggestion {
  @Serializable
  enum class Action {
    UPDATE_ADDRESS,
    MESSAGE
  }

  @Serializable
  data class Redirect(
    val url: String,
    val description: String,
    val buttonTitle: String
  )
}

internal data class TerminationReason(
  val surveyOption: TerminationSurveyOption,
  val feedBack: String?
)
