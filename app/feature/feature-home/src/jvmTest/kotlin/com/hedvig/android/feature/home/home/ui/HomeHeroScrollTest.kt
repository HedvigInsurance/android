package com.hedvig.android.feature.home.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class HomeHeroScrollTest {
  @Test
  fun `a downward wheel scroll collapses the hero`() = runComposeUiTest {
    showHome()
    val open = greetingBounds()

    scrollWheel(1f, times = 10)

    assertThat(greetingBounds().top).isLessThan(open.top - 40.dp)
  }

  @Test
  fun `an upward wheel scroll the list leaves unused opens the collapsed hero again`() = runComposeUiTest {
    showHome()
    val open = greetingBounds()
    scrollWheel(1f, times = 10)
    assertThat(greetingBounds().top).isLessThan(open.top - 40.dp)

    scrollWheel(-1f, times = 10)

    assertThat(greetingBounds()).isEqualTo(open)
  }

  private fun ComposeUiTest.showHome() {
    setContent {
      Box(Modifier.size(width = 400.dp, height = 800.dp)) {
        PreviewHomeScreen(hasUnseenChatMessages = false)
      }
    }
    waitForIdle()
  }

  private fun ComposeUiTest.greetingBounds(): DpRect =
    onNodeWithText("Richard", substring = true, useUnmergedTree = true).getBoundsInRoot()

  private fun ComposeUiTest.scrollWheel(delta: Float, times: Int) {
    repeat(times) {
      onRoot().performMouseInput {
        moveTo(center)
        scroll(delta)
      }
      waitForIdle()
    }
  }
}
