package com.hedvig.android.feature.terminateinsurance.navigation

import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.navigation.compose.typeMapOf
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
)

internal sealed interface TerminateInsuranceDestination {
  @Serializable
  data object StartStep : TerminateInsuranceDestination

  @Serializable
  data class TerminationSurveyFirstStep(
    val options: List<TerminationSurveyOption>,
    val commonParams: TerminationGraphParameters,
  ) : TerminateInsuranceDestination {
    companion object {
      val typeMap = typeMapOf(
        typeOf<List<TerminationSurveyOption>>(),
        typeOf<TerminationGraphParameters>(),
      )
    }
  }

  @Serializable
  data class TerminationSurveySecondStep(
    val subOptions: List<TerminationSurveyOption>,
    val commonParams: TerminationGraphParameters,
  ) : TerminateInsuranceDestination {
    companion object {
      val typeMap = typeMapOf(
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
  ) : TerminateInsuranceDestination {
    companion object {
      val typeMap = typeMapOf(
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
  ) : TerminateInsuranceDestination {
    @Serializable
    sealed interface TerminationType {
      @Serializable
      data object Deletion : TerminationType

      @Serializable
      data class Termination(val terminationDate: LocalDate) : TerminationType
    }

    companion object {
      val typeMap = typeMapOf(
        typeOf<TerminationType>(),
        typeOf<TerminationGraphParameters>(),
      )
    }
  }

  @Serializable
  data class TerminationSuccess(
    val terminationDate: LocalDate?,
  ) : TerminateInsuranceDestination {
    companion object {
      val typeMap = typeMapOf(typeOf<LocalDate?>())
    }
  }

  @Serializable
  data class InsuranceDeletion(val commonParams: TerminationGraphParameters) : TerminateInsuranceDestination {
    companion object {
      val typeMap = typeMapOf(typeOf<TerminationGraphParameters>())
    }
  }

  @Serializable
  data class TerminationFailure(
    val message: String?,
  ) : TerminateInsuranceDestination

  @Serializable
  data object UnknownScreen : TerminateInsuranceDestination
}

@Serializable
internal data class TerminationDateParameters(
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val commonParams: TerminationGraphParameters,
) {
  companion object {
    val typeMap = typeMapOf(
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
