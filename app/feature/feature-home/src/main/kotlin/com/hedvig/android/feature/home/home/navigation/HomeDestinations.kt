package com.hedvig.android.feature.home.home.navigation

import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

sealed interface HomeDestination {
  @Serializable
  data object Graph : HomeDestination, Destination

  @Serializable
  data class Home(
    // When true, the home screen automatically opens the start-claim consent sheet on arrival. Defaults to false for
    // regular home navigation; set to true when arriving via the `/submit-claim` deep link (see homeGraph).
    val startClaimFlow: Boolean = false,
  ) : HomeDestination, Destination

  @Serializable
  data class FirstVet(val sections: List<FirstVetSection>) : HomeDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<FirstVetSection>>())
    }
  }
}

val homeCrossSellBottomSheetPermittingDestinations: List<KClass<out Destination>> = listOf(
  HomeDestination.Home::class,
)
