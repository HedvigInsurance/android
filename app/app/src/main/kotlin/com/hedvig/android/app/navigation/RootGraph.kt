package com.hedvig.android.app.navigation

import com.hedvig.android.navigation.compose.Destination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("root")
object RootGraph : Destination {
  val route = "root"

  override fun toString(): String {
    return route
  }
}
