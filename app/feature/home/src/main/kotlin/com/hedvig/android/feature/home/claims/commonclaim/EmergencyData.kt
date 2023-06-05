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
    fun from(data: HomeQuery.CommonClaim, eligibleToClaim: Boolean): EmergencyData? {
      val layout = data.layout.asEmergency ?: return null
      return EmergencyData(
        ThemedIconUrls.from(data.icon.variants.fragments.iconVariantsFragment),
        layout.color,
        data.title,
        eligibleToClaim,
        layout.emergencyNumber,
      )
    }
  }
}
