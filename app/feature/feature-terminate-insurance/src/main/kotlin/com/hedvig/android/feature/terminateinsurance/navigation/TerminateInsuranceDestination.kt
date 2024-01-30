package com.hedvig.android.feature.terminateinsurance.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

internal sealed interface TerminateInsuranceDestination : Destination {
  @Serializable
  object StartStep : TerminateInsuranceDestination

  @Serializable
  data class TerminationDate(
    val minDate: LocalDate,
    val maxDate: LocalDate,
  ) : TerminateInsuranceDestination

  @Serializable
  data class TerminationOverview(
    val terminationDate: LocalDate,
  ) : TerminateInsuranceDestination

  @Serializable
  data class TerminationSuccess(
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
  object UnknownScreen : TerminateInsuranceDestination
}
