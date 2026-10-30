package com.hedvig.android.feature.claim.chat.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isLessThan
import com.hedvig.android.design.system.hedvig.HedvigTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SentAnswerTextTest {
  @Test
  fun `a wrapped answer's bubble is as wide as its widest line, not as wide as the row`() = runComposeUiTest {
    setContent {
      HedvigTheme(darkTheme = false) {
        Box(Modifier.width(ROW_WIDTH)) {
          SentAnswerRow {
            RoundCornersPill(Modifier.testTag("bubble")) { SentAnswerText(WRAPPING_ANSWER) }
          }
        }
      }
    }
    val bubble = onNodeWithTag("bubble").getUnclippedBoundsInRoot()
    val text = onNodeWithText(WRAPPING_ANSWER).getUnclippedBoundsInRoot()

    // Still a wrapped answer: taller than one line of body text.
    assertThat(text.height).isGreaterThan(30.dp)
    // Narrower than what the row offers, which is the row minus its 48dp start inset.
    assertThat(bubble.width).isLessThan(ROW_WIDTH - 48.dp)
    // And still against the end edge.
    assertThat(bubble.right).isEqualTo(ROW_WIDTH)
  }

  @Test
  fun `a short answer's bubble hugs it as before`() = runComposeUiTest {
    setContent {
      HedvigTheme(darkTheme = false) {
        Box(Modifier.width(ROW_WIDTH)) {
          SentAnswerRow {
            RoundCornersPill(Modifier.testTag("bubble")) { SentAnswerText("Stulen") }
          }
        }
      }
    }
    val bubble = onNodeWithTag("bubble").getUnclippedBoundsInRoot()
    assertThat(bubble.width).isLessThan(120.dp)
    assertThat(bubble.right).isEqualTo(ROW_WIDTH)
  }

  companion object {
    private val ROW_WIDTH = 343.dp
    private const val WRAPPING_ANSWER = "Cykelförråd eller gemensamt utrymme"
  }
}
