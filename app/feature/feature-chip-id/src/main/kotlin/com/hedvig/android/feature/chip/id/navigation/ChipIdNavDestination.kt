package com.hedvig.android.feature.chip.id.navigation

import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChipIdGraphDestination(val contractId: String? = null) : Destination {
  companion object : DestinationNavTypeAware {
    override val typeList: List<KType> = emptyList()
  }
}

internal sealed interface ChipIdDestination {
  @androidx.annotation.Keep
  @Serializable
  data class AddChipId(
    val contractId: String
  ) : ChipIdDestination, Destination

  @androidx.annotation.Keep
  @Serializable
  data object SelectInsuranceForChipId : ChipIdDestination, Destination
  @androidx.annotation.Keep
  @Serializable
  data class AddChipIdTriage(
    /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer.petIdWithContractId] */
    @SerialName("contractId")
    val contractId: String? = null
  ) : ChipIdDestination, Destination
}

