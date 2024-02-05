package com.hedvig.android.core.uidata

import android.net.Uri
import kotlinx.serialization.Serializable

@Serializable
data class UiFile(
  val name: String,
  val path: String,
  val mimeType: String,
  val id: String,
) {
  companion object {
    fun fromUri(uri: Uri, mimeType: String) = UiFile(
      path = uri.toString(),
      mimeType = mimeType,
      id = uri.toString(),
      name = uri.toString(),
    )
  }
}
