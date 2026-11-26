package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.hedvig.android.navigation.common.TopLevelTab
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class NavigationBarHighlightTest {
  /**
   * The app moves the bar between scenes with movableContentOf on every tab switch. After clicking every tab with
   * the mouse and moving the mouse off the bar, the bar must look exactly like one nobody touched.
   */
  @Test
  fun `after clicking each tab of a bar that moves on every switch only the selected tab is highlighted`() =
    runComposeUiTest {
      var selected by mutableStateOf(TopLevelTab.Home)
      var hostedInColumn by mutableStateOf(false)
      setContent {
        HedvigTheme(darkTheme = false) {
          val movingBar = remember {
            movableContentOf {
              NavigationBar(
                destinations = TopLevelTab.entries.toSet(),
                onNavigateToDestination = {
                  selected = it
                  hostedInColumn = !hostedInColumn
                },
                getIsCurrentlySelected = { it == selected },
              )
            }
          }
          Column(Modifier.width(400.dp)) {
            Box(Modifier.testTag(UntouchedBar)) {
              NavigationBar(
                destinations = TopLevelTab.entries.toSet(),
                onNavigateToDestination = {},
                getIsCurrentlySelected = { it == selected },
              )
            }
            Box(Modifier.testTag(MovingBar)) {
              if (hostedInColumn) Column { movingBar() } else Box { movingBar() }
            }
          }
        }
      }

      val tabs = TopLevelTab.entries.indices.map { index ->
        onAllNodes(isTab and hasAnyAncestor(hasTestTag(MovingBar)))[index]
      }
      for (tab in tabs) {
        tab.performMouseInput {
          moveTo(center)
          click(center)
        }
        waitForIdle()
      }
      onNodeWithTag(UntouchedBar).performMouseInput { exit() }
      waitForIdle()

      val untouched = onNodeWithTag(UntouchedBar).captureToImage().toPixelMap()
      val moved = onNodeWithTag(MovingBar).captureToImage().toPixelMap()
      assertEquals(TopLevelTab.entries.last(), selected)
      assertEquals<Int>(untouched.width, moved.width)
      assertEquals<Int>(untouched.height, moved.height)
      val differingPixels = (0 until untouched.width).sumOf { x ->
        (0 until untouched.height).count { y -> untouched[x, y] != moved[x, y] }
      }
      assertEquals(0, differingPixels)
    }

  private val isTab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

  private companion object {
    const val UntouchedBar = "untouched"
    const val MovingBar = "moving"
  }
}
