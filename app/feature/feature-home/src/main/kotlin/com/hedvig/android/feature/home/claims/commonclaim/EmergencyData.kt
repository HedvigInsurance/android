package com.hedvig.android.feature.home.claims.commonclaim

import android.os.Parcelable
import com.hedvig.android.core.common.android.ThemedIconUrls
import kotlinx.parcelize.Parcelize
import octopus.type.HedvigColor

@Parcelize
data class EmergencyData(
  val iconUrls: ThemedIconUrls,
  val color: HedvigColor,
  val title: String,
  val eligibleToClaim: Boolean,
  val emergencyNumber: String,
) : Parcelable
