package com.hedvig.android.feature.terminateinsurance.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

internal sealed interface TerminateInsuranceDestination : Destination {
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
