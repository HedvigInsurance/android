package com.hedvig.android.desktop

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

/**
 * [ProvideVisibleWindowLifecycle] around the Navigation 3 `NavDisplay`, under a window lifecycle the test controls the
 * way Compose desktop sets it: RESUMED while focused, STARTED while visible without focus, CREATED while minimised.
 */
@OptIn(ExperimentalTestApi::class)
class VisibleWindowLifecycleTest {
  private data object First

  private data object Second

  private val backStack = mutableStateListOf<Any>(First)
  private var firstLifecycle: Lifecycle? = null
  private var firstClick: (() -> Unit)? = null
  private var clicks = 0
  private val window = object : LifecycleOwner {
    val registry = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
    override val lifecycle: Lifecycle get() = registry
  }

  private fun ComposeUiTest.show() {
    mainClock.autoAdvance = false
    val navigationEvents = object : NavigationEventDispatcherOwner {
      override val navigationEventDispatcher = NavigationEventDispatcher()
    }
    val transition: () -> ContentTransform = {
      fadeIn(tween(TRANSITION_MILLIS)) togetherWith
        fadeOut(tween(TRANSITION_MILLIS))
    }
    setContent {
      CompositionLocalProvider(
        LocalLifecycleOwner provides window,
        LocalNavigationEventDispatcherOwner provides navigationEvents,
      ) {
        ProvideVisibleWindowLifecycle {
          NavDisplay(
            backStack = backStack,
            transitionSpec = { transition() },
            popTransitionSpec = { transition() },
            entryProvider = { key ->
              NavEntry(key) {
                if (key == First) {
                  val lifecycle = LocalLifecycleOwner.current.lifecycle
                  SideEffect { firstLifecycle = lifecycle }
                  val click = dropUnlessResumed { clicks++ }
                  SideEffect { firstClick = click }
                } else {
                  BasicText("second")
                }
              }
            },
          )
        }
      }
    }
    advance(100)
  }

  private fun ComposeUiTest.advance(millis: Long) {
    mainClock.advanceTimeBy(millis)
    waitForIdle()
  }

  private fun ComposeUiTest.setWindowState(state: Lifecycle.State) {
    runOnIdle { window.registry.currentState = state }
    advance(100)
  }

  private fun ComposeUiTest.clickFirst() {
    runOnIdle { firstClick?.invoke() }
  }

  @Test
  fun `a visible window is resumed with or without focus, and a minimised or closed one keeps its state`() {
    assertThat(Lifecycle.State.RESUMED.asVisibleWindowState()).isEqualTo(Lifecycle.State.RESUMED)
    assertThat(Lifecycle.State.STARTED.asVisibleWindowState()).isEqualTo(Lifecycle.State.RESUMED)
    assertThat(Lifecycle.State.CREATED.asVisibleWindowState()).isEqualTo(Lifecycle.State.CREATED)
    assertThat(Lifecycle.State.DESTROYED.asVisibleWindowState()).isEqualTo(Lifecycle.State.DESTROYED)
    assertThat(Lifecycle.State.INITIALIZED.asVisibleWindowState()).isEqualTo(Lifecycle.State.INITIALIZED)
  }

  @Test
  fun `on a visible window without focus the screen is resumed and a click is not dropped`() = runComposeUiTest {
    window.registry.currentState = Lifecycle.State.STARTED
    show()
    assertThat(firstLifecycle?.currentState).isEqualTo(Lifecycle.State.RESUMED)
    clickFirst()
    assertThat(clicks).isEqualTo(1)
  }

  @Test
  fun `a window losing focus while shown keeps the screen resumed`() = runComposeUiTest {
    show()
    setWindowState(Lifecycle.State.STARTED)
    assertThat(firstLifecycle?.currentState).isEqualTo(Lifecycle.State.RESUMED)
    clickFirst()
    assertThat(clicks).isEqualTo(1)
  }

  @Test
  fun `on a minimised window a click is dropped, and works again once the window is shown again`() = runComposeUiTest {
    show()
    setWindowState(Lifecycle.State.CREATED)
    assertThat(firstLifecycle?.currentState).isEqualTo(Lifecycle.State.CREATED)
    clickFirst()
    assertThat(clicks).isEqualTo(0)

    // Restored without focus.
    setWindowState(Lifecycle.State.STARTED)
    assertThat(firstLifecycle?.currentState).isEqualTo(Lifecycle.State.RESUMED)
    clickFirst()
    assertThat(clicks).isEqualTo(1)

    setWindowState(Lifecycle.State.RESUMED)
    clickFirst()
    assertThat(clicks).isEqualTo(2)
  }

  @Test
  fun `a screen leaving in a transition is below resumed and drops a click, on a window without focus`() =
    runComposeUiTest {
      window.registry.currentState = Lifecycle.State.STARTED
      show()
      runOnIdle { backStack.add(Second) }
      advance(TRANSITION_MILLIS / 2L)
      assertThat(firstLifecycle?.currentState).isEqualTo(Lifecycle.State.STARTED)
      clickFirst()
      assertThat(clicks).isEqualTo(0)
    }

  private companion object {
    const val TRANSITION_MILLIS = 1_000
  }
}
