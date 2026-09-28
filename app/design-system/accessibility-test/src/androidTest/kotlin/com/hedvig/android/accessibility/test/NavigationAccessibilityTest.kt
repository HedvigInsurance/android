package com.hedvig.android.accessibility.test

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.hedvig.android.design.system.hedvig.HedvigTabRow
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.design.system.hedvig.icon.ArrowLeft
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class NavigationAccessibilityTest(private val darkTheme: Boolean) {
  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun topAppBarBack() = composeTestRule.assertAccessible(darkTheme) {
    TopAppBar(actionType = TopAppBarActionType.BACK, onActionClick = {}, title = "Coverage")
  }

  @Test
  fun topAppBarClose() = composeTestRule.assertAccessible(darkTheme) {
    TopAppBar(actionType = TopAppBarActionType.CLOSE, onActionClick = {}, title = "Coverage")
  }

  @Test
  fun topAppBarWithBack() = composeTestRule.assertAccessible(darkTheme) {
    TopAppBarWithBack(onClick = {})
  }

  @Test
  fun tabRow() = composeTestRule.assertAccessible(darkTheme) {
    HedvigTabRow(tabTitles = listOf("Overview", "Coverage", "Documents"))
  }

  @Test
  fun iconButton() = composeTestRule.assertAccessible(darkTheme) {
    IconButton(onClick = {}) {
      Icon(imageVector = HedvigIcons.ArrowLeft, contentDescription = "Back")
    }
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "darkTheme={0}")
    fun themes(): List<Array<Any>> = listOf(arrayOf(false), arrayOf(true))
  }
}
