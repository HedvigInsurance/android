package com.hedvig.android.navigation.core

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

sealed interface TopLevelGraph : Destination {
  @Serializable
  data object HOME : TopLevelGraph

  @Serializable
  data object INSURANCE : TopLevelGraph

  fun toName(): String {
    return when (this) {
      HOME -> "HOME"
      INSURANCE -> "INSURANCE"
      FOREVER -> "REFERRALS"
      PROFILE -> "PROFILE"
    }
  }

  companion object {
    fun fromName(input: String): TopLevelGraph? {
      return when (input) {
        "HOME" -> HOME
        "INSURANCE" -> INSURANCE
        "REFERRALS" -> FOREVER
        "PROFILE" -> PROFILE
        else -> null
      }
    }
  }
}
