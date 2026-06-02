package com.hedvig.android.feature.terminateinsurance.navigation

import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.SuggestionType
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TerminateInsuranceKey(
  @SerialName("contractId")
  val insuranceId: String? = null,
) : HedvigNavKey

@Serializable
internal data class TerminationSurveyFirstStepKey(
  val options: List<TerminationSurveyOption>,
  val action: TerminationAction,
  val commonParams: TerminationGraphParameters,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<List<TerminationSurveyOption>>(),
      typeOf<TerminationAction>(),
      typeOf<TerminationGraphParameters>(),
    )
  }
}

@Serializable
internal data class TerminationSurveySecondStepKey(
  val subOptions: List<TerminationSurveyOption>,
  val action: TerminationAction,
  val commonParams: TerminationGraphParameters,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<List<TerminationSurveyOption>>(),
      typeOf<TerminationAction>(),
      typeOf<TerminationGraphParameters>(),
    )
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
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<LocalDate>(),
      typeOf<List<ExtraCoverageItem>>(),
      typeOf<TerminationGraphParameters>(),
    )
  }
}

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

  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<TerminationType>(),
      typeOf<List<ExtraCoverageItem>>(),
      typeOf<TerminationGraphParameters>(),
    )
  }
}

@Serializable
internal data class InsuranceDeletionKey(
  val commonParams: TerminationGraphParameters,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val selectedReasonId: String,
  val feedbackComment: String?,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<TerminationGraphParameters>(),
      typeOf<List<ExtraCoverageItem>>(),
    )
  }
}

@Serializable
internal data class TerminationSuccessKey(
  val terminationDate: LocalDate?,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(typeOf<LocalDate?>())
  }
}

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
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<SuggestionType>(),
      typeOf<TerminationGraphParameters>(),
      typeOf<TerminationAction>(),
    )
  }
}

@Serializable
internal data class TerminationDateParameters(
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val commonParams: TerminationGraphParameters,
) {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<LocalDate>(),
      typeOf<TerminationGraphParameters>(),
    )
  }
}

@Serializable
internal data class TerminationGraphParameters(
  val contractId: String,
  val insuranceDisplayName: String,
  val exposureName: String,
  val contractGroup: ContractGroup,
)
