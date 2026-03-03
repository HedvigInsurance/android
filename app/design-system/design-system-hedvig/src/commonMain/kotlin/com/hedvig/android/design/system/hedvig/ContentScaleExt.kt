package com.hedvig.android.design.system.hedvig

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor

fun ContentScale.scaledBy(scaleFactor: Float): ContentScale {
  return object : ContentScale {
    override fun computeScaleFactor(srcSize: Size, dstSize: Size): ScaleFactor {
      return this@scaledBy.computeScaleFactor(srcSize, dstSize).times(scaleFactor)
    }
  }
}
