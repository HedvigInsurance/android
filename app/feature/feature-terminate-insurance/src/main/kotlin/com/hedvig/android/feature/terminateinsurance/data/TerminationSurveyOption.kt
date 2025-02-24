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
  sealed interface Known : SurveyOptionSuggestion {
    val description: String
    val infoType: InfoType

    @Serializable
    sealed interface Action : Known {
      val buttonTitle: String

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

      @Serializable
      data class Redirect(
        val url: String,
        override val description: String,
        override val buttonTitle: String,
        override val infoType: InfoType,
      ) : Action
    }

    @Serializable
    data class Info(
      override val description: String,
      override val infoType: InfoType,
    ) : Known
  }

  // Fallback for future-proofing old clients
  @Serializable
  data object Unknown : SurveyOptionSuggestion
}

enum class InfoType {
  INFO,
  OFFER,
  UNKNOWN,
}
