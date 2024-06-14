package com.hedvig.android.feature.forever.navigation

import com.hedvig.android.navigation.compose.Destination
import kotlinx.serialization.Serializable

sealed interface ForeverDestination {
  @Serializable
  data object Graph : ForeverDestination, Destination

  @Serializable
  data object Forever : ForeverDestination, Destination
}
