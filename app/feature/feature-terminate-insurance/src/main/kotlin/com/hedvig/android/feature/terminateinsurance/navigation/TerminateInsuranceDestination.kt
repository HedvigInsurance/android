package com.hedvig.android.feature.terminateinsurance.navigation

import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.SuggestionType
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal data class TerminationSurveyFirstStepKey(
  val options: List<TerminationSurveyOption>,
  val action: TerminationAction,
  val commonParams: TerminationGraphParameters,
) : HedvigNavKey

@Serializable
internal data class TerminationSurveySecondStepKey(
  val subOptions: List<TerminationSurveyOption>,
  val action: TerminationAction,
  val commonParams: TerminationGraphParameters,
) : HedvigNavKey

@Serializable
internal data class TerminationDateKey(
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val commonParams: TerminationGraphParameters,
  val selectedReasonId: String,
  val feedbackComment: String?,
) : HedvigNavKey

@Serializable
internal data class TerminationConfirmationKey(
  val terminationType: TerminationType,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val commonParams: TerminationGraphParameters,
  val selectedReasonId: String,
  val feedbackComment: String?,
) : HedvigNavKey {
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
) : HedvigNavKey

@Serializable
internal data class TerminationSuccessKey(
  val terminationDate: LocalDate?,
) : HedvigNavKey

@Serializable
internal data class TerminationFailureKey(
  val message: String?,
) : HedvigNavKey

@Serializable
internal data object UnknownScreenKey : HedvigNavKey

@Serializable
internal data class DeflectSuggestionKey(
  val description: String,
  val url: String?,
  val suggestionType: SuggestionType,
  val commonParams: TerminationGraphParameters,
  val action: TerminationAction,
  val selectedReasonId: String,
  val feedbackComment: String?,
) : HedvigNavKey

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
