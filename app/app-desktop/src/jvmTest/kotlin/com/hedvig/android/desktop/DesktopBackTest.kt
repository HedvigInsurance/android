package com.hedvig.android.desktop

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.MouseButton
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.text.TextRange
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.desktop.DesktopOs.MacOs
import com.hedvig.android.desktop.DesktopOs.Other
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.navigation.common.HedvigNavKey
import org.junit.Assume
import org.junit.Test

class DesktopBackTest {
  @Test
  fun `the back shortcuts of each operating system go back`() {
    for (os in DesktopOs.entries) {
      assertThat(isBackShortcut(DesktopKeyPress(Key.Escape), os), "Escape on $os").isTrue()
    }
    assertThat(isBackShortcut(DesktopKeyPress(Key.LeftBracket, isMetaPressed = true), MacOs)).isTrue()
    assertThat(isBackShortcut(DesktopKeyPress(Key.DirectionLeft, isMetaPressed = true), MacOs)).isTrue()
    assertThat(isBackShortcut(DesktopKeyPress(Key.DirectionLeft, isAltPressed = true), Other)).isTrue()
  }

  @Test
  fun `other keys and the other system's shortcuts do not go back`() {
    val notBack = mapOf(
      MacOs to listOf(
        DesktopKeyPress(Key.DirectionLeft),
        DesktopKeyPress(Key.DirectionLeft, isAltPressed = true),
        DesktopKeyPress(Key.DirectionLeft, isMetaPressed = true, isShiftPressed = true),
        DesktopKeyPress(Key.LeftBracket),
        DesktopKeyPress(Key.LeftBracket, isMetaPressed = true, isShiftPressed = true),
        DesktopKeyPress(Key.RightBracket, isMetaPressed = true),
        DesktopKeyPress(Key.Escape, isShiftPressed = true),
        DesktopKeyPress(Key.Backspace),
      ),
      Other to listOf(
        DesktopKeyPress(Key.DirectionLeft),
        DesktopKeyPress(Key.DirectionLeft, isCtrlPressed = true),
        DesktopKeyPress(Key.DirectionLeft, isAltPressed = true, isShiftPressed = true),
        DesktopKeyPress(Key.DirectionLeft, isMetaPressed = true),
        DesktopKeyPress(Key.LeftBracket, isMetaPressed = true),
        DesktopKeyPress(Key.LeftBracket, isCtrlPressed = true),
        DesktopKeyPress(Key.Escape, isCtrlPressed = true),
        DesktopKeyPress(Key.Backspace),
      ),
    )
    for ((os, presses) in notBack) {
      for (press in presses) {
        assertThat(isBackShortcut(press, os), "$press on $os").isFalse()
      }
    }
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun `Escape on a deeper screen pops it`() = runComposeUiTest {
    val backstack = backstackOn(HomeKey, InsurancesKey)
    showApp(backstack, DesktopBackInput(Other))

    onNodeWithTag(ContentTag).performKeyInput { pressKey(Key.Escape) }
    waitForIdle()

    assertThat(backstack.entries.toList()).containsExactly(HomeKey)
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun `a back shortcut at the root changes nothing and does not quit`() = runComposeUiTest {
    val backstack = backstackOn(HomeKey)
    var quit = false
    backstack.finishApp = { quit = true }
    showApp(backstack, DesktopBackInput(Other))

    onNodeWithTag(ContentTag).performKeyInput { pressKey(Key.Escape) }
    onNodeWithTag(ContentTag).performKeyInput { withKeyDown(Key.AltLeft) { pressKey(Key.DirectionLeft) } }
    onRoot().performMouseInput {
      moveTo(center)
      press(MouseButtonBack)
      release(MouseButtonBack)
    }
    waitForIdle()

    assertThat(backstack.entries.toList()).containsExactly(HomeKey)
    assertThat(quit).isFalse()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun `the mouse back button pops a deeper screen`() = runComposeUiTest {
    val backstack = backstackOn(HomeKey, InsurancesKey)
    showApp(backstack, DesktopBackInput(Other))

    onRoot().performMouseInput {
      moveTo(center)
      press(MouseButtonBack)
      release(MouseButtonBack)
    }
    waitForIdle()

    assertThat(backstack.entries.toList()).containsExactly(HomeKey)
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun `with a text field focused, Cmd+Left moves the caret and Cmd+left bracket still goes back`() = runComposeUiTest {
    // The text field's key bindings follow the machine running the test; Cmd+Left is a macOS text binding.
    Assume.assumeTrue(DesktopOs.current == MacOs)
    val backstack = backstackOn(HomeKey, InsurancesKey)
    val text = TextFieldState("Some text").apply { edit { placeCursorAtEnd() } }
    showApp(backstack, DesktopBackInput(MacOs), text)
    onNodeWithTag(TextFieldTag).requestFocus()
    waitForIdle()

    onNodeWithTag(TextFieldTag).performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.DirectionLeft) } }
    waitForIdle()

    assertThat(text.selection).isEqualTo(TextRange(0))
    assertThat(backstack.entries.toList()).containsExactly(HomeKey, InsurancesKey)

    onNodeWithTag(TextFieldTag).performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.LeftBracket) } }
    waitForIdle()

    assertThat(backstack.entries.toList()).containsExactly(HomeKey)
  }

  private fun backstackOn(vararg keys: HedvigNavKey): BackstackController =
    newBackstackController().apply { seedIfEmpty(keys.toList()) }

  /**
   * The window wiring of the desktop app: [backInput] added to the window's navigation event dispatcher, key events
   * reaching it after the focused element had them (as the window's `onKeyEvent` does), and the mouse back button,
   * around a NavDisplay whose back handler pops [backstack] the way the shell's does. A test scene delivers key events
   * only to a focused element and its ancestors, so the content itself is focusable and focused unless a text field
   * takes the focus; the window also receives the keys while nothing is focused.
   */
  @OptIn(ExperimentalTestApi::class)
  private fun ComposeUiTest.showApp(
    backstack: BackstackController,
    backInput: DesktopBackInput,
    text: TextFieldState? = null,
  ) {
    val owner = object : NavigationEventDispatcherOwner {
      override val navigationEventDispatcher = NavigationEventDispatcher()
    }
    setContent {
      CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
        DisposableEffect(Unit) {
          owner.navigationEventDispatcher.addInput(backInput)
          onDispose { owner.navigationEventDispatcher.removeInput(backInput) }
        }
        Column(
          Modifier
            .fillMaxSize()
            .onKeyEvent(backInput::handle)
            .mouseBackButton(backInput)
            .testTag(ContentTag)
            .focusable(),
        ) {
          if (text != null) {
            BasicTextField(text, Modifier.testTag(TextFieldTag))
          }
          NavDisplay(
            backStack = backstack.entries,
            onBack = { backstack.popBackstack() },
            entryProvider = { key -> NavEntry(key) { BasicText(key.toString(), Modifier.fillMaxSize()) } },
          )
        }
      }
    }
    waitForIdle()
    if (text == null) {
      onNodeWithTag(ContentTag).requestFocus()
      waitForIdle()
    }
  }

  private companion object {
    const val TextFieldTag = "text field"
    const val ContentTag = "content"
    val MouseButtonBack = MouseButton(3)
  }
}
