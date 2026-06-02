package com.hedvig.android.feature.forever.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

sealed interface ForeverDestination {
  @Serializable
  data object Graph : ForeverDestination, HedvigNavKey

  @Serializable
  data object Forever : ForeverDestination, HedvigNavKey
}
