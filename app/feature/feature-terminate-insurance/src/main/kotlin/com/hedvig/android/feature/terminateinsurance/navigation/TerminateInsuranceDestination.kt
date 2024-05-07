package com.hedvig.android.feature.terminateinsurance.navigation

import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.kiwi.navigationcompose.typed.Destination
import java.io.Serial
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

internal sealed interface TerminateInsuranceDestination : Destination {
  @Serializable
  data object StartStep : TerminateInsuranceDestination

  @Serializable
  data class TerminationSurveyFirstStep(
    val options: List<TerminationSurveyOption>
  ): TerminateInsuranceDestination

  @Serializable
  data class TerminationSurveySecondStep(
    val subOptions: List<TerminationSurveyOption>
  ): TerminateInsuranceDestination

  @Serializable
  data class TerminationDate(
    val minDate: LocalDate,
    val maxDate: LocalDate,
    val insuranceDisplayName: String,
    val exposureName: String,
    val activeFrom: LocalDate,
    val contractGroup: ContractGroup,
  ) : TerminateInsuranceDestination

  /**
   * The screen to review the termination situation before submitting the final request
   */
  @Serializable
  data class TerminationConfirmation(
    val terminationType: TerminationType,
    val parameters: TerminationConfirmationParameters,
  ) : TerminateInsuranceDestination {
    @Serializable
    sealed interface TerminationType {
      @Serializable
      data object Deletion : TerminationType

      @Serializable
      data class Termination(val terminationDate: LocalDate) : TerminationType
    }
  }

  @Serializable
  data class TerminationSuccess(
    val insuranceDisplayName: String,
    val exposureName: String,
    val terminationDate: LocalDate?,
    val surveyUrl: String,
  ) : TerminateInsuranceDestination

  @Serializable
  data class InsuranceDeletion(
    val insuranceDisplayName: String,
    val exposureName: String,
    val activeFrom: LocalDate,
    val contractGroup: ContractGroup,
  ) : TerminateInsuranceDestination

  @Serializable
  data class TerminationFailure(
    val message: String?,
  ) : TerminateInsuranceDestination

  @Serializable
  data object UnknownScreen : TerminateInsuranceDestination
}

@Serializable
internal data class TerminationDataParameters(
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val insuranceDisplayName: String,
  val exposureName: String,
)

@Serializable
internal data class TerminationConfirmationParameters(
  val insuranceDisplayName: String,
  val exposureName: String,
  val activeFrom: LocalDate,
  val contractGroup: ContractGroup,
)
