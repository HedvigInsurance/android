package com.hedvig.android.feature.terminateinsurance.navigation

import com.hedvig.android.data.contract.ContractGroup
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class TerminateInsuranceFeatureDestination(
  val contractId: String,
  val insuranceDisplayName: String,
  val exposureName: String,
  val contractGroup: ContractGroup,
  val activeFrom: LocalDate,
)

internal sealed interface TerminateInsuranceDestination {
  @Serializable
  data object StartStep : TerminateInsuranceDestination

  @Serializable
  data class TerminationDate(
    val minDate: LocalDate,
    val maxDate: LocalDate,
  ) : TerminateInsuranceDestination

  /**
   * The screen to review the termination situation before submitting the final request
   */
  @Serializable
  data class TerminationReview(val terminationType: TerminationType) : TerminateInsuranceDestination {
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
    val disclaimer: String,
  ) : TerminateInsuranceDestination

  @Serializable
  data class TerminationFailure(
    val message: String?,
  ) : TerminateInsuranceDestination

  @Serializable
  data object UnknownScreen : TerminateInsuranceDestination
}
