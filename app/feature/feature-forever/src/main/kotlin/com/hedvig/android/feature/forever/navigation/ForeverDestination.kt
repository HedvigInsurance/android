package com.hedvig.android.feature.forever.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

sealed interface ForeverDestination {
  @Serializable
  data object Graph : ForeverDestination, Destination

  @Serializable
  data object Forever : ForeverDestination, Destination
}
