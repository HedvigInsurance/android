package com.hedvig.android.app.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("root")
object RootGraph {
  val route = "root"

  override fun toString(): String {
    return route
  }
}
