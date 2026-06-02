package com.hedvig.android.feature.login.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
data object LoginDestination : HedvigNavKey

internal sealed interface LoginDestinations {
  @Serializable
  object Marketing : LoginDestinations, HedvigNavKey

  @Serializable
  object SwedishLogin : LoginDestinations, HedvigNavKey

  /**
   * The screen where the Qasa email is added for SE
   */
  @Serializable
  object GenericAuthCredentialsInput : LoginDestinations, HedvigNavKey

  /**
   * The screen where the OTP coming from email is entered in order to login to the app
   */
  @Serializable
  data class OtpInput(
    val otpInformation: OtpInformation,
  ) : LoginDestinations, HedvigNavKey {
    @Serializable
    data class OtpInformation(
      val verifyUrl: String,
      val resendUrl: String,
      val credential: String,
    )

    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(typeOf<OtpInformation>())
    }
  }
}
