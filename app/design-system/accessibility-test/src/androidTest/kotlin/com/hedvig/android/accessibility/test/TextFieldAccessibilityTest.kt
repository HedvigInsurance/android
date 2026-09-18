package com.hedvig.android.accessibility.test

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TextFieldAccessibilityTest(
  private val textFieldSize: HedvigTextFieldDefaults.TextFieldSize,
  private val darkTheme: Boolean,
) {
  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun emptyTextField() = composeTestRule.assertAccessible(darkTheme) {
    HedvigTextField(
      text = "",
      onValueChange = {},
      labelText = "Street address",
      textFieldSize = textFieldSize,
    )
  }

  @Test
  fun filledTextField() = composeTestRule.assertAccessible(darkTheme) {
    HedvigTextField(
      text = "Bellmansgatan 19",
      onValueChange = {},
      labelText = "Street address",
      textFieldSize = textFieldSize,
    )
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "{0}, darkTheme={1}")
    fun cases(): List<Array<Any>> = HedvigTextFieldDefaults.TextFieldSize.entries.flatMap { size ->
      listOf(arrayOf<Any>(size, false), arrayOf<Any>(size, true))
    }
  }
}
