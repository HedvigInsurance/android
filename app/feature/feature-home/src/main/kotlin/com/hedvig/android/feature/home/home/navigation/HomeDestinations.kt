package com.hedvig.android.feature.home.home.navigation

import com.hedvig.android.navigation.compose.typeMapOf
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

sealed interface HomeDestination {
  @Serializable
  data object Graph : HomeDestination

  @Serializable
  data object Home : HomeDestination

  @Serializable
  data class FirstVet(val sections: List<FirstVetSection>) : HomeDestination {
    companion object {
      val typeMap = typeMapOf(typeOf<List<FirstVetSection>>())
    }
  }
}
