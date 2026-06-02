package com.hedvig.android.app.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("root")
object RootGraph : HedvigNavKey {
  val route = "root"

  override fun toString(): String {
    return route
  }
}
