package com.hedvig.android.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.Test

class DesktopWindowSizeTest {
  @Test
  fun `a screen taller than the preferred window keeps the preferred size`() {
    assertThat(initialWindowSize(usableScreenHeight = 1_355.dp)).isEqualTo(DpSize(412.dp, 915.dp))
  }

  @Test
  fun `a laptop screen lowers the window to the usable height`() {
    assertThat(initialWindowSize(usableScreenHeight = 875.dp)).isEqualTo(DpSize(412.dp, 875.dp))
  }

  @Test
  fun `an unknown screen size keeps the preferred size`() {
    assertThat(initialWindowSize(usableScreenHeight = null)).isEqualTo(DpSize(412.dp, 915.dp))
  }

  @Test
  fun `without a screen there is no usable height`() {
    assertThat(usableScreenHeight()).isNull()
  }
}
