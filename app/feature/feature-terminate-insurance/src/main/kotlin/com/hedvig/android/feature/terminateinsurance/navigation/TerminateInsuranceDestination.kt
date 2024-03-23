package com.hedvig.android.feature.terminateinsurance.navigation

import com.hedvig.android.data.contract.ContractGroup
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TerminateInsuranceFeatureDestination(
  @SerialName("insuranceId")
  val insuranceId: String?,
) : Destination

internal sealed interface TerminateInsuranceDestination : Destination {
  @Serializable
  data object StartStep : TerminateInsuranceDestination

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
  data class TerminationReview(
    val parameters: TerminationReviewViewModelParameters,
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
internal data class TerminationReviewViewModelParameters(
  val activeFrom: LocalDate,
  val insuranceDisplayName: String,
  val exposureName: String,
  val contractGroup: ContractGroup,
  val terminationType: TerminateInsuranceDestination.TerminationReview.TerminationType,
)
