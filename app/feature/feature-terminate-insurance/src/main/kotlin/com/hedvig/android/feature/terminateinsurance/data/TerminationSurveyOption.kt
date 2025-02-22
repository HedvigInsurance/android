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
  val isDisabled: Boolean = false,
)

@Serializable
internal sealed interface SurveyOptionSuggestion {
  val description: String
  val buttonTitle: String?
  val infoType: InfoType

  @Serializable
  data class Info(
    override val description: String,
    override val infoType: InfoType,
  ) : SurveyOptionSuggestion {
    override val buttonTitle = null
  }

  @Serializable
  sealed interface Action : SurveyOptionSuggestion {
    @Serializable
    data class UpdateAddress(
      override val description: String,
      override val buttonTitle: String,
      override val infoType: InfoType,
    ) : Action

    @Serializable
    data class UpgradeCoverageByChangingTier(
      override val description: String,
      override val buttonTitle: String,
      override val infoType: InfoType,
    ) : Action

    @Serializable
    data class DowngradePriceByChangingTier(
      override val description: String,
      override val buttonTitle: String,
      override val infoType: InfoType,
    ) : Action

    @Serializable // adding for filtering. may be useful in the future for old clients?
    data object UnknownAction : Action {
      override val description: String = ""
      override val buttonTitle: String = ""
      override val infoType: InfoType = InfoType.UNKNOWN
    }
  }

  @Serializable
  data class Redirect(
    val url: String,
    override val description: String,
    override val buttonTitle: String,
    override val infoType: InfoType,
  ) : SurveyOptionSuggestion
}

enum class InfoType {
  INFO,
  OFFER,
  UNKNOWN,
}
