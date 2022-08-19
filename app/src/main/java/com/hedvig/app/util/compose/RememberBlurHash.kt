package com.hedvig.app.util.compose

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import com.hedvig.app.util.BlurHashDecoder

@Composable
fun rememberBlurHashPainter(
  blurHash: String,
  width: Int,
  height: Int,
  context: Context = LocalContext.current,
): Painter? = remember(blurHash, width, height, context) {
  blurHash(blurHash, width, height, context)?.toPainter()
}

@Composable
fun rememberBlurHash(
  blurHash: String,
  width: Int,
  height: Int,
  context: Context = LocalContext.current,
): BitmapDrawable? = remember(blurHash, width, height, context) {
  blurHash(blurHash, width, height, context)
}

fun blurHash(blurHash: String, width: Int, height: Int, context: Context): BitmapDrawable? {
  return BlurHashDecoder.decode(blurHash, width, height)?.let { decodedBitmap ->
    BitmapDrawable(
      context.resources,
      decodedBitmap,
    )
  }
}

fun BitmapDrawable.toPainter(): Painter {
  return BitmapPainter(bitmap.asImageBitmap())
}
