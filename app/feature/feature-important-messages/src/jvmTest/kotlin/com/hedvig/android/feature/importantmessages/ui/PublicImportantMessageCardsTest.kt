package com.hedvig.android.feature.importantmessages.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.hedvig.android.data.importantmessages.PublicImportantMessage
import com.hedvig.android.data.importantmessages.PublicImportantMessage.Link
import com.hedvig.android.data.importantmessages.PublicImportantMessage.Severity
import com.hedvig.android.design.system.hedvig.HedvigTheme
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class PublicImportantMessageCardsTest {
  @Test
  fun `clicking a card's link button opens the link`() = runComposeUiTest {
    val opened = mutableListOf<String>()
    setContent {
      HedvigTheme(darkTheme = false) {
        PublicImportantMessageCards(
          messages = listOf(
            PublicImportantMessage(
              id = "error-1",
              message = "Logins are failing",
              severity = Severity.Error,
              link = Link(url = "https://www.hedvig.com/se/hjalp", buttonText = "See status"),
            ),
          ),
          openUrl = { opened += it },
        )
      }
    }

    onNodeWithText("See status").performClick()

    assertThat(opened).containsExactly("https://www.hedvig.com/se/hjalp")
  }

  @Test
  fun `no messages take no space`() = runComposeUiTest {
    setContent {
      HedvigTheme(darkTheme = false) {
        Column(Modifier.testTag("container")) {
          PublicImportantMessageCards(messages = emptyList(), openUrl = {}, modifier = Modifier.size(100.dp))
        }
      }
    }

    val bounds = onNodeWithTag("container").getBoundsInRoot()
    assertThat(bounds.bottom - bounds.top).isEqualTo(0.dp)
  }
}
