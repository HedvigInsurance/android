package com.hedvig.android.feature.imageviewer

import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import androidx.core.graphics.createBitmap
import coil3.Bitmap

internal actual fun Bitmap.onWhiteBackground(): Bitmap {
  // Without an alpha channel there is no transparency for the white background to show through, so compositing
  // would allocate a second full-size copy of a visually identical image.
  if (!hasAlpha()) return this
  val output = createBitmap(width, height)
  val canvas = AndroidCanvas(output)
  canvas.drawColor(AndroidColor.WHITE)
  canvas.drawBitmap(this, 0f, 0f, null)
  return output
}
