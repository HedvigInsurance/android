package com.hedvig.android.feature.chip.id.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChipIdKey(val contractId: String? = null) : HedvigNavKey

@androidx.annotation.Keep
@Serializable
internal data class AddChipIdKey(
  val contractId: String,
) : HedvigNavKey

@androidx.annotation.Keep
@Serializable
internal data class AddChipIdTriageKey(
  /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer.petIdWithContractId] */
  @SerialName("contractId")
  val contractId: String? = null,
) : HedvigNavKey
