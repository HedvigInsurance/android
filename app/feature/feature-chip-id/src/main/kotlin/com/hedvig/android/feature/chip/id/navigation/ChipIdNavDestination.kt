package com.hedvig.android.feature.chip.id.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChipIdGraphDestination(val contractId: String? = null) : HedvigNavKey

internal sealed interface ChipIdDestination {
  @androidx.annotation.Keep
  @Serializable
  data class AddChipId(
    val contractId: String,
  ) : ChipIdDestination, HedvigNavKey

  @androidx.annotation.Keep
  @Serializable
  data class AddChipIdTriage(
    /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer.petIdWithContractId] */
    @SerialName("contractId")
    val contractId: String? = null,
  ) : ChipIdDestination, HedvigNavKey
}
