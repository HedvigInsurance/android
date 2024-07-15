package com.hedvig.android.feature.terminateinsurance.data

import kotlinx.serialization.Serializable

@Serializable
internal data class TerminationSurveyOption(
  val id: String,
  val listIndex: Int,
  val title: String,
  val feedBackRequired: Boolean,
  val suggestion: SurveyOptionSuggestion?,
  val subOptions: List<TerminationSurveyOption>,
)

@Serializable
internal sealed interface SurveyOptionSuggestion {
  val description: String
  val buttonTitle: String

  @Serializable
  sealed interface Action : SurveyOptionSuggestion {
    @Serializable
    data class UpdateAddress(
      override val description: String,
      override val buttonTitle: String,
    ) : Action
  }

  @Serializable
  data class Redirect(
    val url: String,
    override val description: String,
    override val buttonTitle: String,
  ) : SurveyOptionSuggestion
}

internal data class TerminationReason(
  val surveyOption: TerminationSurveyOption,
  val feedBack: String?,
)
