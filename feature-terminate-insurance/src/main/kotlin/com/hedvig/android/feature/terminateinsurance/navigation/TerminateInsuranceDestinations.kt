package com.hedvig.android.feature.terminateinsurance.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface Destinations : Destination {
  @Serializable
  object TerminateInsurance : Destinations
}

internal sealed interface TerminateInsuranceDestinations : Destination {
  @Serializable
  object TerminationDate : TerminateInsuranceDestinations

  @Serializable
  object TerminationSuccess : TerminateInsuranceDestinations
}
