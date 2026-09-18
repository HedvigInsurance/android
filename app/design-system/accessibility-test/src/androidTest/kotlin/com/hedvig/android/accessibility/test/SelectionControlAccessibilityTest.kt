package com.hedvig.android.accessibility.test

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.hedvig.android.design.system.hedvig.Checkbox
import com.hedvig.android.design.system.hedvig.CheckboxOption
import com.hedvig.android.design.system.hedvig.HedvigToggle
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SelectionControlAccessibilityTest(private val darkTheme: Boolean) {
  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private val options = listOf(
    RadioOption(RadioOptionId("home"), "Home insurance"),
    RadioOption(RadioOptionId("travel"), "Travel insurance"),
  )

  @Test
  fun radioGroupLarge() = composeTestRule.assertAccessible(darkTheme) {
    RadioGroup(options, RadioOptionId("home"), {}, size = RadioGroupSize.Large)
  }

  @Test
  fun radioGroupMedium() = composeTestRule.assertAccessible(darkTheme) {
    RadioGroup(options, RadioOptionId("home"), {}, size = RadioGroupSize.Medium)
  }

  @Test
  fun radioGroupSmall() = composeTestRule.assertAccessible(darkTheme) {
    RadioGroup(options, RadioOptionId("home"), {}, size = RadioGroupSize.Small)
  }

  @Test
  fun radioGroupWithUnselectedOptions() = composeTestRule.assertAccessible(darkTheme) {
    RadioGroup(options, selectedOption = null, onRadioOptionSelected = {})
  }

  @Test
  fun checkboxSelected() = composeTestRule.assertAccessible(darkTheme) {
    Checkbox(CheckboxOption("I agree to the terms"), selected = true, onCheckboxSelected = {})
  }

  @Test
  fun checkboxUnselected() = composeTestRule.assertAccessible(darkTheme) {
    Checkbox(CheckboxOption("I agree to the terms"), selected = false, onCheckboxSelected = {})
  }

  @Test
  fun toggleOn() = composeTestRule.assertAccessible(darkTheme) {
    HedvigToggle(labelText = "Push notifications", turnedOn = true, onClick = {}, enabled = true)
  }

  @Test
  fun toggleOff() = composeTestRule.assertAccessible(darkTheme) {
    HedvigToggle(labelText = "Push notifications", turnedOn = false, onClick = {}, enabled = true)
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "darkTheme={0}")
    fun themes(): List<Array<Any>> = listOf(arrayOf(false), arrayOf(true))
  }
}
