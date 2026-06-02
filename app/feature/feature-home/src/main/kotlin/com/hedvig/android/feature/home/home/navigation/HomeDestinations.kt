package com.hedvig.android.feature.home.home.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

sealed interface HomeDestination {
  @Serializable
  data object Graph : HomeDestination, HedvigNavKey

  @Serializable
  data object Home : HomeDestination, HedvigNavKey

  @Serializable
  data class FirstVet(val sections: List<FirstVetSection>) : HomeDestination, HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<FirstVetSection>>())
    }
  }
}

val homeCrossSellBottomSheetPermittingDestinations: List<KClass<out HedvigNavKey>> = listOf(
  HomeDestination.Home::class,
)
