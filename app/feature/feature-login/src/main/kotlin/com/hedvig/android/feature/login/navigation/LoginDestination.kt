package com.hedvig.android.feature.login.navigation

import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
data object LoginDestination : Destination

internal sealed interface LoginDestinations {
  @Serializable
  object Marketing : LoginDestinations, Destination

  @Serializable
  object SwedishLogin : LoginDestinations, Destination

  /**
   * The screen where the Qasa email is added for SE
   */
  @Serializable
  object GenericAuthCredentialsInput : LoginDestinations, Destination

  /**
   * The screen where the OTP coming from email is entered in order to login to the app
   */
  @Serializable
  data class OtpInput(
    val otpInformation: OtpInformation,
  ) : LoginDestinations, Destination {
    @Serializable
    data class OtpInformation(
      val verifyUrl: String,
      val resendUrl: String,
      val credential: String,
    )

    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<OtpInformation>())
    }
  }
}
