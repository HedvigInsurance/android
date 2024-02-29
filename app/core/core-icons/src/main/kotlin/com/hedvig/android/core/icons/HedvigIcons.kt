package com.hedvig.android.core.icons

import androidx.compose.material.icons.Icons
import com.hedvig.android.core.icons.hedvig.normal.Document
import com.hedvig.android.core.icons.hedvig.normal.Pictures
import com.hedvig.android.core.icons.hedvig.normal.Play

@Suppress("UnusedReceiverParameter")
val Icons.Hedvig: HedvigIcons
  get() = HedvigIcons

object HedvigIcons

fun getIconFromMimeType(mimeType: String) = when (mimeType) {
  "image/jpg" -> Icons.Hedvig.Pictures
  "video/quicktime" -> Icons.Hedvig.Play
  "application/pdf" -> Icons.Hedvig.Document
  else -> Icons.Hedvig.Document
}
