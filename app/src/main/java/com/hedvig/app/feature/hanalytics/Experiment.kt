package com.hedvig.app.feature.hanalytics

import kotlinx.serialization.Serializable

@Serializable
data class Experiment(
  val name: String,
  val variant: String,
)
