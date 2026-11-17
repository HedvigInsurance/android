package com.hedvig.android.feature.home.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.pan
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTrackpadInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import org.junit.Test

/**
 * Wheel deltas and trackpad pan offsets are sent with Compose's own sign: positive y scrolls toward the end of the
 * list, negative y toward the top.
 *
 * How far one wheel notch scrolls depends on the operating system the tests run on: Compose for Desktop scrolls 10 dp
 * per notch on macOS, the square root of the viewport height in pixels on Linux, and a twentieth of the viewport
 * height on Windows. So the tests never scroll a fixed number of notches to collapse the hero. They scroll one notch at
 * a time until the hero has collapsed, which leaves the greeting in the list on every platform.
 */
@OptIn(ExperimentalTestApi::class)
class HomeHeroScrollTest {
  /** Whether the latest wheel event reached the screen's parent still unconsumed. */
  private var latestWheelScrollWasUnused: Boolean? = null

  @Test
  fun `a downward wheel scroll collapses the hero`() = runComposeUiTest {
    showHome()
    val open = greetingBounds()

    collapseHeroWithWheel(open)

    assertThat(greetingBounds().top).isLessThan(open.top - 40.dp)
  }

  @Test
  fun `an upward wheel scroll the list leaves unused opens the collapsed hero again`() = runComposeUiTest {
    showHome()
    val open = greetingBounds()
    val notches = collapseHeroWithWheel(open)
    assertThat(greetingBounds().top).isLessThan(open.top - 40.dp)

    scrollWheel(Offset(0f, -1f), times = notches)

    assertThat(greetingBounds()).isEqualTo(open)
  }

  @Test
  fun `at the end of the list a wheel scroll toward the end is left unused`() = runComposeUiTest {
    showHome()
    scrollWheel(Offset(0f, 1f), times = 60)

    scrollWheel(Offset(0f, 1f), times = 1)

    assertThat(latestWheelScrollWasUnused).isEqualTo(true)
  }

  @Test
  fun `a mostly horizontal wheel scroll with a slight upward part does not open the collapsed hero`() =
    runComposeUiTest {
      showHome()
      val open = greetingBounds()
      collapseHeroWithWheel(open)
      val collapsed = greetingBounds()
      assertThat(collapsed.top).isLessThan(open.top - 40.dp)

      scrollWheel(Offset(-3f, -0.5f), times = 3)

      assertThat(greetingBounds()).isEqualTo(collapsed)
    }

  @Test
  fun `a trackpad pan toward the end collapses the hero and one toward the top opens it again`() = runComposeUiTest {
    showHome()
    val open = greetingBounds()

    pan(Offset(0f, 30f), times = 3)
    assertThat(greetingBounds().top).isLessThan(open.top - 40.dp)

    pan(Offset(0f, -30f), times = 6)

    assertThat(greetingBounds()).isEqualTo(open)
  }

  private fun ComposeUiTest.showHome() {
    setContent {
      Box(
        Modifier
          .size(width = 400.dp, height = 800.dp)
          .pointerInput(Unit) {
            awaitPointerEventScope {
              while (true) {
                val event = awaitPointerEvent()
                if (event.type == PointerEventType.Scroll) {
                  latestWheelScrollWasUnused = !event.changes.first().isConsumed
                }
              }
            }
          },
      ) {
        PreviewHomeScreen(hasUnseenChatMessages = false)
      }
    }
    waitForIdle()
  }

  private fun ComposeUiTest.greetingBounds(): DpRect =
    onNodeWithText("Richard", substring = true, useUnmergedTree = true).getBoundsInRoot()

  /**
   * Scrolls the wheel toward the end one notch at a time until the greeting sits more than 40 dp above [open], and
   * returns how many notches that took. Stopping there keeps the greeting in the list whatever one notch scrolls.
   */
  private fun ComposeUiTest.collapseHeroWithWheel(open: DpRect): Int {
    var notches = 0
    while (greetingBounds().top >= open.top - 40.dp) {
      check(notches < 10) { "The hero did not collapse after $notches wheel notches" }
      scrollWheel(Offset(0f, 1f), times = 1)
      notches++
    }
    return notches
  }

  private fun ComposeUiTest.scrollWheel(offset: Offset, times: Int) {
    repeat(times) {
      onRoot().performMouseInput {
        moveTo(center)
        scroll(offset)
      }
      waitForIdle()
    }
  }

  private fun ComposeUiTest.pan(offset: Offset, times: Int) {
    repeat(times) {
      onRoot().performTrackpadInput {
        moveTo(center)
        pan(offset)
      }
      waitForIdle()
    }
  }
}
