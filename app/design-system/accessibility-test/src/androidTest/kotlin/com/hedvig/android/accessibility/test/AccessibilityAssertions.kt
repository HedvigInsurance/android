package com.hedvig.android.accessibility.test

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.hedvig.android.design.system.hedvig.HedvigTheme

/**
 * Renders [content] in [HedvigTheme] and runs the Accessibility Test Framework over the result, the
 * same engine Accessibility Scanner and the Play pre-launch report use. It covers content labelling,
 * touch target size, colour contrast and traversal order; a violation throws and fails the test.
 *
 * Contrast results depend on the rendered pixels, so a component is worth checking in both themes.
 */
internal fun AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.assertAccessible(
  darkTheme: Boolean,
  content: @Composable () -> Unit,
) {
  setContent {
    HedvigTheme(darkTheme = darkTheme) {
      content()
    }
  }
  enableAccessibilityChecks()
  onRoot().tryPerformAccessibilityChecks()
}
