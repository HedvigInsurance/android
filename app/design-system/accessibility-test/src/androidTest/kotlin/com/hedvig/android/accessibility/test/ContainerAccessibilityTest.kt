package com.hedvig.android.accessibility.test

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.hedvig.android.design.system.hedvig.AccordionData
import com.hedvig.android.design.system.hedvig.AccordionList
import com.hedvig.android.design.system.hedvig.HedvigBigCard
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ContainerAccessibilityTest(private val darkTheme: Boolean) {
  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun card() = composeTestRule.assertAccessible(darkTheme) {
    HedvigCard {
      HedvigText("Your home insurance")
    }
  }

  @Test
  fun bigCardWithLabelAndInput() = composeTestRule.assertAccessible(darkTheme) {
    HedvigBigCard(onClick = {}, labelText = "Move-in date", inputText = "12 March 2026")
  }

  @Test
  fun bigCardWithEmptyInput() = composeTestRule.assertAccessible(darkTheme) {
    HedvigBigCard(onClick = {}, labelText = "Move-in date", inputText = null)
  }

  @Test
  fun accordionList() = composeTestRule.assertAccessible(darkTheme) {
    AccordionList(
      items = listOf(
        AccordionData("What is covered?", "Your home and its contents."),
        AccordionData("How do I claim?", "Start a claim from the home screen."),
      ),
    )
  }

  @Test
  fun highlightLabelLarge() = composeTestRule.assertAccessible(darkTheme) {
    HighlightLabel("Active", HighLightSize.Large, HighlightColor.Green(HighlightShade.LIGHT))
  }

  @Test
  fun highlightLabelMedium() = composeTestRule.assertAccessible(darkTheme) {
    HighlightLabel("Active", HighLightSize.Medium, HighlightColor.Blue(HighlightShade.MEDIUM))
  }

  @Test
  fun highlightLabelSmall() = composeTestRule.assertAccessible(darkTheme) {
    HighlightLabel("Active", HighLightSize.Small, HighlightColor.Red(HighlightShade.DARK))
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "darkTheme={0}")
    fun themes(): List<Array<Any>> = listOf(arrayOf(false), arrayOf(true))
  }
}
