package com.hedvig.android.desktop

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isBackPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigationevent.NavigationEventInput

/** The operating system families whose back shortcuts differ. */
internal enum class DesktopOs {
  MacOs,
  Other,
  ;

  companion object {
    val current: DesktopOs
      get() = if (System.getProperty("os.name").orEmpty().startsWith("Mac")) MacOs else Other
  }
}

/** A key press, reduced to what decides whether it is a back shortcut. */
internal data class DesktopKeyPress(
  val key: Key,
  val isMetaPressed: Boolean = false,
  val isAltPressed: Boolean = false,
  val isCtrlPressed: Boolean = false,
  val isShiftPressed: Boolean = false,
)

/**
 * Whether [press] is a back shortcut on [os]: Escape everywhere, Cmd+[ and Cmd+Left on macOS, and Alt+Left elsewhere,
 * the shortcuts of Finder, Explorer and the browsers. On macOS a focused text field handles Cmd+Left (line start)
 * before the window does, so it goes back only while no text field takes it. Text fields on Windows and Linux bind
 * Ctrl+Left, not Alt+Left, so Alt+Left goes back even while typing, as it does in a browser.
 */
internal fun isBackShortcut(press: DesktopKeyPress, os: DesktopOs): Boolean {
  val noModifiers = !press.isMetaPressed && !press.isAltPressed && !press.isCtrlPressed && !press.isShiftPressed
  val metaOnly = press.isMetaPressed && !press.isAltPressed && !press.isCtrlPressed && !press.isShiftPressed
  val altOnly = press.isAltPressed && !press.isMetaPressed && !press.isCtrlPressed && !press.isShiftPressed
  return when {
    press.key == Key.Escape -> noModifiers
    os == DesktopOs.MacOs -> metaOnly && (press.key == Key.LeftBracket || press.key == Key.DirectionLeft)
    else -> altOnly && press.key == Key.DirectionLeft
  }
}

/**
 * The desktop's system back. While a back handler is enabled (a screen deeper than the root, a sheet, a dialog), a
 * back shortcut or the mouse back button is delivered to the window's back handlers like the Android back gesture. At
 * the root it does nothing, so the app only quits when its window closes. Holding a back key repeats it, going back
 * one screen per key repeat.
 */
internal class DesktopBackInput(private val os: DesktopOs = DesktopOs.current) : NavigationEventInput() {
  private var hasEnabledHandlers = false

  override fun onHasEnabledHandlersChanged(hasEnabledHandlers: Boolean) {
    this.hasEnabledHandlers = hasEnabledHandlers
  }

  /** Handles a window key event, returning whether it was a back shortcut. */
  fun handle(event: KeyEvent): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    val press = DesktopKeyPress(
      key = event.key,
      isMetaPressed = event.isMetaPressed,
      isAltPressed = event.isAltPressed,
      isCtrlPressed = event.isCtrlPressed,
      isShiftPressed = event.isShiftPressed,
    )
    if (!isBackShortcut(press, os)) return false
    back()
    return true
  }

  fun back() {
    if (hasEnabledHandlers) dispatchOnBackCompleted()
  }
}

/** Goes back through [backInput] when the mouse back button is pressed anywhere in the content. */
internal fun Modifier.mouseBackButton(backInput: DesktopBackInput): Modifier = pointerInput(backInput) {
  awaitPointerEventScope {
    var backButtonWasDown = false
    while (true) {
      val event = awaitPointerEvent(PointerEventPass.Initial)
      val backButtonIsDown = event.buttons.isBackPressed
      if (event.type == PointerEventType.Press && backButtonIsDown && !backButtonWasDown) {
        event.changes.forEach { it.consume() }
        backInput.back()
      }
      backButtonWasDown = backButtonIsDown
    }
  }
}
