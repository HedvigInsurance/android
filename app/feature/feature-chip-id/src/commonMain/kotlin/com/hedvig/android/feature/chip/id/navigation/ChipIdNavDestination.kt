package com.hedvig.android.feature.chip.id.navigation

import com.hedvig.android.core.common.KeepKmp
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@KeepKmp
@Serializable
data class ChipIdKey(
  @SerialName("contractId")
  val contractId: String? = null,
) : HedvigNavKey

@KeepKmp
@Serializable
internal data class AddChipIdKey(
  val contractId: String,
) : HedvigNavKey
