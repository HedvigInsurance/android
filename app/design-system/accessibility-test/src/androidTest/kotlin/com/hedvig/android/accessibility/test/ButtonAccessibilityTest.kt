package com.hedvig.android.accessibility.test

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ButtonAccessibilityTest(
  private val buttonSize: ButtonDefaults.ButtonSize,
  private val darkTheme: Boolean,
) {
  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun buttonPassesAccessibilityChecks() {
    composeTestRule.assertAccessible(darkTheme) {
      HedvigButton(
        text = "Continue",
        onClick = {},
        enabled = true,
        buttonSize = buttonSize,
      )
    }
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "{0}, darkTheme={1}")
    fun cases(): List<Array<Any>> = ButtonDefaults.ButtonSize.entries.flatMap { size ->
      listOf(arrayOf<Any>(size, false), arrayOf<Any>(size, true))
    }
  }
}
