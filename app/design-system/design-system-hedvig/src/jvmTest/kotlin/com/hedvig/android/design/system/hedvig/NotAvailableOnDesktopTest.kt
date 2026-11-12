package com.hedvig.android.design.system.hedvig

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class NotAvailableOnDesktopTest {
  @Test
  fun `shows the not available on desktop text`() = runComposeUiTest {
    setContent {
      HedvigTheme(darkTheme = false) {
        NotAvailableOnDesktop()
      }
    }
    onNodeWithText("Not available on desktop").assertIsDisplayed()
  }
}
