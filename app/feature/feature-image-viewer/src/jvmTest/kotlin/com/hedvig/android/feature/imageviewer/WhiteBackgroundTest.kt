package com.hedvig.android.feature.imageviewer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color
import org.jetbrains.skia.IRect

class WhiteBackgroundTest {
  @Test
  fun `a transparent pixel comes out white and an opaque pixel is unchanged`() {
    val red = Color.makeARGB(255, 255, 0, 0)
    val input = Bitmap().apply {
      allocN32Pixels(2, 1)
      erase(Color.TRANSPARENT)
      erase(red, IRect.makeXYWH(1, 0, 1, 1))
    }

    val output = input.onWhiteBackground()

    assertEquals(Color.WHITE, output.getColor(0, 0))
    assertEquals(red, output.getColor(1, 0))
  }

  @Test
  fun `an image without transparency is returned as is`() {
    val input = Bitmap().apply {
      allocN32Pixels(1, 1, true)
      erase(Color.BLACK)
    }

    assertSame(input, input.onWhiteBackground())
  }
}
