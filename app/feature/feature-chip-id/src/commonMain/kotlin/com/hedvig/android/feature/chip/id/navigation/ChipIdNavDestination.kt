package com.hedvig.android.feature.chip.id.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@androidx.annotation.Keep
@Serializable
data class ChipIdKey(
  @SerialName("contractId")
  val contractId: String? = null,
) : HedvigNavKey

@androidx.annotation.Keep
@Serializable
internal data class AddChipIdKey(
  val contractId: String,
) : HedvigNavKey
