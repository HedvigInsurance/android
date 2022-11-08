package com.hedvig.android.hanalytics

import kotlinx.serialization.Serializable

@InternalHanalyticsApi
@Serializable
data class Experiment(
  val name: String,
  val variant: String,
)
