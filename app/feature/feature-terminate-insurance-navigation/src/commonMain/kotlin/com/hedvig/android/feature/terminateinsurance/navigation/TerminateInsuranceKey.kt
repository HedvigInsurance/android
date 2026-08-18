package com.hedvig.android.feature.terminateinsurance.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TerminateInsuranceKey(
  @SerialName("contractId")
  val insuranceId: String? = null,
) : HedvigNavKey
