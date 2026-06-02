package com.hedvig.android.feature.login.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
data object LoginKey : HedvigNavKey

@Serializable
internal data object SwedishLoginKey : HedvigNavKey

/**
 * The screen where the Qasa email is added for SE
 */
@Serializable
internal data object GenericAuthCredentialsInputKey : HedvigNavKey

/**
 * The screen where the OTP coming from email is entered in order to login to the app
 */
@Serializable
internal data class OtpInputKey(
  val otpInformation: OtpInformation,
) : HedvigNavKey {
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
