package com.hedvig.android.feature.login.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface LoginDestination : Destination {
  @Serializable
  object Marketing : LoginDestination

  @Serializable
  object SwedishLogin : LoginDestination
}
