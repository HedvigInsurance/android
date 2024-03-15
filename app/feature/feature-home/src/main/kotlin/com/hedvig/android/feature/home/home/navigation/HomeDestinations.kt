package com.hedvig.android.feature.home.home.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination : Destination

internal sealed interface HomeDestinations : Destination {
  @Serializable
  data object Home : HomeDestinations
}
