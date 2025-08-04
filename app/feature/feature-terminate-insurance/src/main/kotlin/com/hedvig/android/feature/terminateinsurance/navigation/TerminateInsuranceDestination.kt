package com.hedvig.android.feature.terminateinsurance.navigation

import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.TerminationNotification
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
  /**
   * The ID to the contract which needs to be pre-selected in the termination flow
   */
  @SerialName("contractId")
  val insuranceId: String? = null,
) : Destination

internal sealed interface TerminateInsuranceDestination {
  @Serializable
  data object StartStep : TerminateInsuranceDestination, Destination

  @Serializable
  data class TerminationSurveyFirstStep(
    val options: List<TerminationSurveyOption>,
    val commonParams: TerminationGraphParameters,
  ) : TerminateInsuranceDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<List<TerminationSurveyOption>>(),
        typeOf<TerminationGraphParameters>(),
      )
    }
  }

  @Serializable
  data class TerminationSurveySecondStep(
    val subOptions: List<TerminationSurveyOption>,
    val commonParams: TerminationGraphParameters,
  ) : TerminateInsuranceDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<List<TerminationSurveyOption>>(),
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
  ) : TerminateInsuranceDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<LocalDate>(),
        typeOf<List<ExtraCoverageItem>>(),
        typeOf<TerminationNotification?>(),
        typeOf<TerminationGraphParameters>(),
      )
    }
  }

  /**
   * The screen to review the termination situation before submitting the final request
   */
  @Serializable
  data class TerminationConfirmation(
    val terminationType: TerminationType,
    val extraCoverageItems: List<ExtraCoverageItem>,
    val commonParams: TerminationGraphParameters,
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
        typeOf<TerminationNotification?>(),
        typeOf<TerminationGraphParameters>(),
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
  data class InsuranceDeletion(
    val commonParams: TerminationGraphParameters,
    val extraCoverageItems: List<ExtraCoverageItem>,
  ) : TerminateInsuranceDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<TerminationGraphParameters>(),
        typeOf<List<ExtraCoverageItem>>(),
      )
    }
  }

  @Serializable
  data class TerminationFailure(
    val message: String?,
  ) : TerminateInsuranceDestination, Destination

  @Serializable
  data object UnknownScreen : TerminateInsuranceDestination, Destination
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
