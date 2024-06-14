package com.hedvig.android.feature.terminateinsurance.navigation

import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.navigation.compose.Destination
import com.hedvig.android.navigation.compose.DestinationNavTypeAware
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
  val insuranceId: String?,
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
    val commonParams: TerminationGraphParameters,
  ) : TerminateInsuranceDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<LocalDate>(),
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
  ) : TerminateInsuranceDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<TerminationGraphParameters>())
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
  val insuranceDisplayName: String,
  val exposureName: String,
)
