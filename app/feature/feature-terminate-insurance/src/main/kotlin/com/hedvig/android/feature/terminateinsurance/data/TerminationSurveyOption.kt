package com.hedvig.android.feature.terminateinsurance.data

internal data class TerminationSurveyOption(
  val id: String,
  val title: String,
  val feedBackRequired: Boolean,
  val suggestion: SurveyOptionSuggestion,
  val subOptions: List<TerminationSurveyOption>
)

internal sealed interface SurveyOptionSuggestion {
  enum class Action {
    UPDATE_ADDRESS,
    MESSAGE
  }

  data class Redirect(
    val url: String,
    val description: String,
    val buttonTitle: String
  )
}
