package com.hedvig.android.feature.terminateinsurance.navigation

import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.SuggestionType
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionRedirection
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.navigation.common.AnalyticsNamed
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal data class TerminationSurveyFirstStepKey(
  val options: List<TerminationSurveyOption>,
  val action: TerminationAction,
  val commonParams: TerminationGraphParameters,
) : HedvigNavKey, AnalyticsNamed {
  override val analyticsName get() = ANALYTICS_NAME

  companion object {
    const val ANALYTICS_NAME = "com.hedvig.android.feature.terminateinsurance.navigation.TerminationSurveyFirstStepKey"
  }
}

@Serializable
internal data class TerminationSurveySecondStepKey(
  val subOptions: List<TerminationSurveyOption>,
  val action: TerminationAction,
  val commonParams: TerminationGraphParameters,
) : HedvigNavKey, AnalyticsNamed {
  override val analyticsName get() = ANALYTICS_NAME

  companion object {
    const val ANALYTICS_NAME = "com.hedvig.android.feature.terminateinsurance.navigation.TerminationSurveySecondStepKey"
  }
}

@Serializable
internal data class TerminationRedirectionKey(
  val redirection: SurveyOptionRedirection,
  val selectedOption: TerminationSurveyOption,
  val action: TerminationAction,
  val commonParams: TerminationGraphParameters,
  val feedbackComment: String?,
) : HedvigNavKey, AnalyticsNamed {
  override val analyticsName get() = ANALYTICS_NAME

  companion object {
    const val ANALYTICS_NAME = "com.hedvig.android.feature.terminateinsurance.navigation.TerminationRedirectionKey"
  }
}

@Serializable
internal data class TerminationDateKey(
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val commonParams: TerminationGraphParameters,
  val selectedReasonId: String,
  val feedbackComment: String?,
) : HedvigNavKey, AnalyticsNamed {
  override val analyticsName get() = ANALYTICS_NAME

  companion object {
    const val ANALYTICS_NAME = "com.hedvig.android.feature.terminateinsurance.navigation.TerminationDateKey"
  }
}

@Serializable
internal data class TerminationConfirmationKey(
  val terminationType: TerminationType,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val commonParams: TerminationGraphParameters,
  val selectedReasonId: String,
  val feedbackComment: String?,
) : HedvigNavKey, AnalyticsNamed {
  override val analyticsName get() = ANALYTICS_NAME

  companion object {
    const val ANALYTICS_NAME = "com.hedvig.android.feature.terminateinsurance.navigation.TerminationConfirmationKey"
  }

  @Serializable
  sealed interface TerminationType {
    @Serializable
    data object Deletion : TerminationType

    @Serializable
    data class Termination(val terminationDate: LocalDate) : TerminationType
  }
}

@Serializable
internal data class InsuranceDeletionKey(
  val commonParams: TerminationGraphParameters,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val selectedReasonId: String,
  val feedbackComment: String?,
) : HedvigNavKey, AnalyticsNamed {
  override val analyticsName get() = ANALYTICS_NAME

  companion object {
    const val ANALYTICS_NAME = "com.hedvig.android.feature.terminateinsurance.navigation.InsuranceDeletionKey"
  }
}

@Serializable
internal data class TerminationSuccessKey(
  val terminationDate: LocalDate?,
) : HedvigNavKey, AnalyticsNamed {
  override val analyticsName get() = ANALYTICS_NAME

  companion object {
    const val ANALYTICS_NAME = "com.hedvig.android.feature.terminateinsurance.navigation.TerminationSuccessKey"
  }
}

@Serializable
internal data class TerminationFailureKey(
  val message: String?,
) : HedvigNavKey, AnalyticsNamed {
  override val analyticsName get() = ANALYTICS_NAME

  companion object {
    const val ANALYTICS_NAME = "com.hedvig.android.feature.terminateinsurance.navigation.TerminationFailureKey"
  }
}

@Serializable
internal data object UnknownScreenKey : HedvigNavKey, AnalyticsNamed {
  override val analyticsName get() = ANALYTICS_NAME

  const val ANALYTICS_NAME = "com.hedvig.android.feature.terminateinsurance.navigation.UnknownScreenKey"
}

@Serializable
internal data class DeflectSuggestionKey(
  val description: String,
  val url: String?,
  val suggestionType: SuggestionType,
  val commonParams: TerminationGraphParameters,
  val action: TerminationAction,
  val selectedReasonId: String,
  val feedbackComment: String?,
) : HedvigNavKey, AnalyticsNamed {
  override val analyticsName get() = ANALYTICS_NAME

  companion object {
    const val ANALYTICS_NAME = "com.hedvig.android.feature.terminateinsurance.navigation.DeflectSuggestionKey"
  }
}

@Serializable
internal data class TerminationDateParameters(
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val commonParams: TerminationGraphParameters,
)

@Serializable
internal data class TerminationGraphParameters(
  val contractId: String,
  val insuranceDisplayName: String,
  val exposureName: String,
  val contractGroup: ContractGroup,
)
