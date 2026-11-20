package com.hedvig.android.feature.login.marketing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isLessThanOrEqualTo
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.language.Language
import org.junit.Test

/** A small landscape window at twice the font size, with more message cards than fit above the buttons. */
@OptIn(ExperimentalTestApi::class)
class MarketingScreenOverflowTest {
  private var loginClicks = 0

  @Test
  fun `the login button stays fully on screen and clickable`() = runComposeUiTest {
    showCrowdedLoginScreen()

    val loginButton = onNodeWithTag("login_button")
    loginButton.assertIsDisplayed().assertIsEnabled().assertHasClickAction()
    val bounds = loginButton.getBoundsInRoot()
    assertThat(bounds.top).isGreaterThanOrEqualTo(0.dp)
    assertThat(bounds.bottom).isLessThanOrEqualTo(WINDOW_HEIGHT)
    loginButton.performClick()
    assertThat(loginClicks).isEqualTo(1)
  }

  @Test
  fun `the last card can be scrolled into view without moving the login button`() = runComposeUiTest {
    showCrowdedLoginScreen()
    val buttonBefore = onNodeWithTag("login_button").getBoundsInRoot()

    onNodeWithText(cardText(CARD_COUNT)).performScrollTo().assertIsDisplayed()

    assertThat(onNodeWithTag("login_button").getBoundsInRoot()).isEqualTo(buttonBefore)
  }

  private fun ComposeUiTest.showCrowdedLoginScreen() {
    setContent {
      val density = LocalDensity.current
      CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
        Box(Modifier.size(WINDOW_WIDTH, WINDOW_HEIGHT)) {
          MarketingScreen(
            uiState = MarketingUiState.Success(
              Language.EN_SE,
              isBankIdSandboxAvailable = false,
              isBankIdSandboxEnabled = false,
            ),
            appVersionName = "X.Y.Z",
            selectLanguage = {},
            setBankIdSandboxEnabled = {},
            openWebOnboarding = {},
            navigateToLoginScreen = { loginClicks++ },
            publicImportantMessages = { _, modifier ->
              Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (number in 1..CARD_COUNT) {
                  HedvigNotificationCard(
                    message = cardText(number),
                    priority = NotificationPriority.Error,
                    modifier = Modifier.fillMaxWidth(),
                    style = InfoCardStyle.Button(buttonText = "See status", onButtonClick = {}),
                  )
                }
              }
            },
          )
        }
      }
    }
    waitForIdle()
  }
}

private fun cardText(number: Int) =
  "Message $number: logging in with BankID is slower than usual and some members cannot log in at all. " +
    "We are working on it and will update this message when it is fixed."

private const val CARD_COUNT = 5
private val WINDOW_WIDTH = 640.dp
private val WINDOW_HEIGHT = 360.dp
