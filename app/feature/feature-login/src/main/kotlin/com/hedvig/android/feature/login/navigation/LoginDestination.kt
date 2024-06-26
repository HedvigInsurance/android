package com.hedvig.android.feature.login.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

@Serializable
data object LoginDestination : Destination

internal sealed interface LoginDestinations : Destination {
  @Serializable
  object Marketing : LoginDestinations

  @Serializable
  object SwedishLogin : LoginDestinations

  /**
   * The screen where the Qasa email is added for SE, the SSN for NO and the CPR for DK
   */
  @Serializable
  object GenericAuthCredentialsInput : LoginDestinations

  /**
   * The screen where the OTP coming from email is entered in order to login to the app
   */
  @Serializable
  data class OtpInput(
    val otpInformation: OtpInformation,
  ) : LoginDestinations {
    @Serializable
    data class OtpInformation(
      val verifyUrl: String,
      val resendUrl: String,
      val credential: String,
    )
  }
}
