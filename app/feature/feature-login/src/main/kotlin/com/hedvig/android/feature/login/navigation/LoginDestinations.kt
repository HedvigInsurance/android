package com.hedvig.android.feature.login.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface LoginDestinations : Destination {
  @Serializable
  object Marketing : LoginDestinations

  @Serializable
  object SwedishLogin : LoginDestinations
}
