package com.hedvig.android.feature.insurances.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface InsurancesDestination : Destination {
  @Serializable
  data class InsuranceContractDetail(
    val contractId: String,
  ) : InsurancesDestination

  @Serializable
  object TerminatedInsurances : InsurancesDestination
}
