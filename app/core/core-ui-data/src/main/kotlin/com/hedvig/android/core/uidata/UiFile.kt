package com.hedvig.android.core.uidata

import kotlinx.serialization.Serializable

@Serializable
data class UiFile(
  val name: String,
  val path: String,
  val mimeType: String,
  val id: String,
)

