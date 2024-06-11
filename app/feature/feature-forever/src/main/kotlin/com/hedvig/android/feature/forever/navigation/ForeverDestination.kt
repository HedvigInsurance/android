package com.hedvig.android.feature.forever.navigation

import kotlinx.serialization.Serializable

sealed interface ForeverDestination {
  @Serializable
  data object Graph : ForeverDestination

  @Serializable
  data object Forever : ForeverDestination
}
