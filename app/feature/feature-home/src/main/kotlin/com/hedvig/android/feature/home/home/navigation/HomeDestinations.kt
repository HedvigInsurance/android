package com.hedvig.android.feature.home.home.navigation

import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

sealed interface HomeDestination {
  @Serializable
  data object Graph : HomeDestination, Destination

  @Serializable
  data object Home : HomeDestination, Destination

  @Serializable
  data class FirstVet(val sections: List<FirstVetSection>) : HomeDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<FirstVetSection>>())
    }
  }
}
