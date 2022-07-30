package com.hedvig.android.hanalytics

import kotlinx.serialization.Serializable

@Serializable
internal data class Experiment(
  val name: String,
  val variant: String,
)
