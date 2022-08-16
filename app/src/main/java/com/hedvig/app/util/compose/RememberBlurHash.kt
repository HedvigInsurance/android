package com.hedvig.app.util.compose

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.hedvig.app.util.BlurHashDecoder

@Composable
fun rememberBlurHash(
  blurHash: String,
  width: Int,
  height: Int,
  context: Context = LocalContext.current,
): BitmapDrawable? = remember(blurHash, width, height, context) {
  BlurHashDecoder.decode(blurHash, width, height)?.let { decodedBitmap ->
    BitmapDrawable(
      context.resources,
      decodedBitmap,
    )
  }
}
