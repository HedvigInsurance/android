package com.hedvig.android.feature.home.home.navigation

import com.hedvig.android.ui.emergency.FirstVetSection
import kotlinx.serialization.Serializable

sealed interface HomeDestination {
  @Serializable
  data object Graph : HomeDestination, Destination

  @Serializable
  data object Home : HomeDestination, Destination

  @Serializable
  data class FirstVet(val sections: List<FirstVetSection>) :
    HomeDestination, Destination
}
