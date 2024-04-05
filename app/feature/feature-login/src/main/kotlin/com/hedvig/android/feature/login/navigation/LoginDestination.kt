package com.hedvig.android.feature.login.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Login")
data object LoginGraph

internal sealed interface LoginDestination {
  @Serializable
  object Marketing : LoginDestination

  @Serializable
  object SwedishLogin : LoginDestination
}
