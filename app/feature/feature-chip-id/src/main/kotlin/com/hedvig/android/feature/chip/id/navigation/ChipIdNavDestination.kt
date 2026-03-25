package com.hedvig.android.feature.chip.id.navigation

import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChipIdGraphDestination(val contractId: String) : Destination {
  companion object : DestinationNavTypeAware {
    override val typeList: List<KType> = listOf(typeOf<String>())
  }
}

internal sealed interface ChipIdDestination {
  @Serializable
  data object AddChipId : ChipIdDestination, Destination
}
