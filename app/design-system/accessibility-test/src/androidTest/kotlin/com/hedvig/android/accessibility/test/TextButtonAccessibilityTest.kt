package com.hedvig.android.accessibility.test

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.hedvig.android.design.system.hedvig.HedvigButtonGhostWithBorder
import com.hedvig.android.design.system.hedvig.HedvigRedTextButton
import com.hedvig.android.design.system.hedvig.HedvigSecondaryRedTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TextButtonAccessibilityTest(private val darkTheme: Boolean) {
  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun textButton() = composeTestRule.assertAccessible(darkTheme) {
    HedvigTextButton(text = "Continue", onClick = {})
  }

  @Test
  fun redTextButton() = composeTestRule.assertAccessible(darkTheme) {
    HedvigRedTextButton(text = "Delete", onClick = {})
  }

  @Test
  fun secondaryRedTextButton() = composeTestRule.assertAccessible(darkTheme) {
    HedvigSecondaryRedTextButton(text = "Delete", onClick = {})
  }

  @Test
  fun ghostButtonWithBorder() = composeTestRule.assertAccessible(darkTheme) {
    HedvigButtonGhostWithBorder(text = "Continue", onClick = {})
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "darkTheme={0}")
    fun themes(): List<Array<Any>> = listOf(arrayOf(false), arrayOf(true))
  }
}
