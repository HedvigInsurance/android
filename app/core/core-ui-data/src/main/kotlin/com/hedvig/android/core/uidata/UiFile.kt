package com.hedvig.android.core.uidata

import kotlinx.serialization.Serializable

@Serializable
data class UiFile(
  val name: String,
  val localPath: String?,
  val url: String?,
  val mimeType: String,
  val id: String,
  val thumbnailUrl: String?,
)
