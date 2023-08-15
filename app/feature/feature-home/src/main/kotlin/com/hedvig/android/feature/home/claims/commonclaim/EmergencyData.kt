package com.hedvig.android.feature.home.claims.commonclaim

import android.os.Parcelable
import com.hedvig.android.core.common.android.ThemedIconUrls
import giraffe.HomeQuery
import giraffe.type.HedvigColor
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmergencyData(
  val iconUrls: ThemedIconUrls,
  val color: HedvigColor,
  val title: String,
  val eligibleToClaim: Boolean,
  val emergencyNumber: String,
) : Parcelable {
  companion object {
    fun from(data: HomeQuery.Data): EmergencyData? {
      val emergencyCommonClaim = data.commonClaims.firstOrNull { it.layout.asEmergency != null }
      val emergency: HomeQuery.AsEmergency = emergencyCommonClaim?.layout?.asEmergency ?: return null
      return EmergencyData(
        iconUrls = ThemedIconUrls.from(emergencyCommonClaim.icon.variants.fragments.iconVariantsFragment),
        color = emergency.color,
        title = emergencyCommonClaim.title,
        eligibleToClaim = data.isEligibleToCreateClaim,
        emergencyNumber = emergency.emergencyNumber,
      )
    }
  }
}
