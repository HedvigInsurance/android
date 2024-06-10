package com.hedvig.android.feature.login.navigation

import com.hedvig.android.navigation.compose.typeMapOf
import kotlinx.serialization.Serializable

@Serializable
data object LoginDestination

internal sealed interface LoginDestinations {
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

    companion object {
      val typeMap = typeMapOf<OtpInformation>()
    }
  }
}
