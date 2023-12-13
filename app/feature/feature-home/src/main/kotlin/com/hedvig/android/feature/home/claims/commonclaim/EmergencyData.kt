package com.hedvig.android.feature.home.claims.commonclaim

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyData(
  val title: String,
  val emergencyNumber: String,
)
