package com.hedvig.android.feature.imageviewer

import coil3.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Image

internal actual fun Bitmap.onWhiteBackground(): Bitmap {
  // An opaque image has no transparency for the white background to show through.
  if (isOpaque) return this
  val output = Bitmap()
  output.allocN32Pixels(width, height)
  Canvas(output).use { canvas ->
    canvas.clear(Color.WHITE)
    Image.makeFromBitmap(this).use { image -> canvas.drawImage(image, 0f, 0f) }
  }
  return output
}
