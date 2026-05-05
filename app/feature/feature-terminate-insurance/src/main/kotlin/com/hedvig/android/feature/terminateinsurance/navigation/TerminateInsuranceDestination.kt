package com.hedvig.android.feature.terminateinsurance.navigation

import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.SuggestionType
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TerminateInsuranceGraphDestination(
  @SerialName("contractId")
  val insuranceId: String? = null,
) : Destination

internal sealed interface TerminateInsuranceDestination {
  @Serializable
  data object StartStep : TerminateInsuranceDestination, Destination

  @Serializable
  data class TerminationSurveyFirstStep(
    val options: List<TerminationSurveyOption>,
    val action: TerminationAction,
    val commonParams: TerminationGraphParameters,
  ) : TerminateInsuranceDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<List<TerminationSurveyOption>>(),
        typeOf<TerminationAction>(),
        typeOf<TerminationGraphParameters>(),
      )
    }
  }

  @Serializable
  data class TerminationSurveySecondStep(
    val subOptions: List<TerminationSurveyOption>,
    val action: TerminationAction,
    val commonParams: TerminationGraphParameters,
  ) : TerminateInsuranceDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<List<TerminationSurveyOption>>(),
        typeOf<TerminationAction>(),
        typeOf<TerminationGraphParameters>(),
      )
    }
  }

  @Serializable
  data class TerminationDate(
    val minDate: LocalDate,
    val maxDate: LocalDate,
    val extraCoverageItems: List<ExtraCoverageItem>,
    val commonParams: TerminationGraphParameters,
    val selectedReasonId: String,
    val feedbackComment: String?,
  ) : TerminateInsuranceDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<LocalDate>(),
        typeOf<List<ExtraCoverageItem>>(),
        typeOf<TerminationGraphParameters>(),
      )
    }
  }

  @Serializable
  data class TerminationConfirmation(
    val terminationType: TerminationType,
    val extraCoverageItems: List<ExtraCoverageItem>,
    val commonParams: TerminationGraphParameters,
    val selectedReasonId: String,
    val feedbackComment: String?,
  ) : TerminateInsuranceDestination, Destination {
    @Serializable
    sealed interface TerminationType {
      @Serializable
      data object Deletion : TerminationType

      @Serializable
      data class Termination(val terminationDate: LocalDate) : TerminationType
    }

    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<TerminationType>(),
        typeOf<List<ExtraCoverageItem>>(),
        typeOf<TerminationGraphParameters>(),
      )
    }
  }

  @Serializable
  data class InsuranceDeletion(
    val commonParams: TerminationGraphParameters,
    val extraCoverageItems: List<ExtraCoverageItem>,
    val selectedReasonId: String,
    val feedbackComment: String?,
  ) : TerminateInsuranceDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<TerminationGraphParameters>(),
        typeOf<List<ExtraCoverageItem>>(),
      )
    }
  }

  @Serializable
  data class TerminationSuccess(
    val terminationDate: LocalDate?,
  ) : TerminateInsuranceDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<LocalDate?>())
    }
  }

  @Serializable
  data class TerminationFailure(
    val message: String?,
  ) : TerminateInsuranceDestination, Destination

  @Serializable
  data object UnknownScreen : TerminateInsuranceDestination, Destination

  @Serializable
  data class DeflectSuggestion(
    val description: String,
    val url: String?,
    val suggestionType: SuggestionType,
    val commonParams: TerminationGraphParameters,
    val action: TerminationAction,
    val selectedReasonId: String,
    val feedbackComment: String?,
  ) : TerminateInsuranceDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<SuggestionType>(),
        typeOf<TerminationGraphParameters>(),
        typeOf<TerminationAction>(),
      )
    }
  }
}

@Serializable
internal data class TerminationDateParameters(
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val commonParams: TerminationGraphParameters,
) {
  companion object : DestinationNavTypeAware {
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
