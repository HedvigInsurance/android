package com.hedvig.android.feature.home.emergency

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyData(
  val title: String,
  val emergencyNumber: String,
)
