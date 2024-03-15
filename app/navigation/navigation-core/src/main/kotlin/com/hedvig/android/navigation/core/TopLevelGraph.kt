package com.hedvig.android.navigation.core

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

sealed interface TopLevelGraph : Destination {
  @Serializable
  data object HOME : TopLevelGraph

  fun toName(): String {
    return when (this) {
      HOME -> "HOME"
    }
  }

  companion object {
    fun fromName(input: String): TopLevelGraph? {
      return when (input) {
        "HOME" -> HOME
        else -> null
      }
    }
  }
}
