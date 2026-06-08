package com.hedvig.android.feature.editcoinsured.navigation

import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoInsuredAddInfoKey(
  val contractId: String,
  val type: CoInsuredFlowType,
) : HedvigNavKey

@Serializable
data class CoInsuredAddOrRemoveKey(
  val contractId: String,
  val type: CoInsuredFlowType,
) : HedvigNavKey

@Serializable
data class EditCoInsuredTriageKey(
  @SerialName("contractId")
  val contractId: String? = null,
  val type: CoInsuredFlowType = CoInsuredFlowType.CoInsured,
) : HedvigNavKey
