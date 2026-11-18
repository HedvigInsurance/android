package com.hedvig.android.compose.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.hedvig.android.molecule.public.NavigationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * [NavigationEffect] inside the Navigation 3 `NavDisplay` the desktop app runs, under a window lifecycle the test
 * controls: the desktop window is RESUMED only while focused and STARTED while visible without focus.
 */
@OptIn(ExperimentalTestApi::class)
class NavigationEffectInNavDisplayTest {
  private data object First

  private data object Second

  private val backStack = mutableStateListOf<Any>(First)
  private var request by mutableStateOf<NavigationRequest<String>?>(null)
  private var firstLifecycle: Lifecycle? = null
  private val window = object : LifecycleOwner {
    val registry = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
    override val lifecycle: Lifecycle get() = registry
  }

  @Before
  fun setUp() {
    // Waiting for a lifecycle state dispatches to Dispatchers.Main, which the JVM has no platform implementation of.
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
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
        NavDisplay(
          backStack = backStack,
          transitionSpec = { transition() },
          popTransitionSpec = { transition() },
          entryProvider = { key ->
            NavEntry(key) {
              if (key == First) {
                val lifecycle = LocalLifecycleOwner.current.lifecycle
                SideEffect { firstLifecycle = lifecycle }
                NavigationEffect(request) { backStack.add(Second) }
              } else {
                BasicText("second")
              }
            }
          },
        )
      }
    }
    advance(100)
  }

  private fun ComposeUiTest.advance(millis: Long) {
    mainClock.advanceTimeBy(millis)
    waitForIdle()
  }

  @Test
  fun `a request that reaches a screen leaving in a transition is not performed there`() = runComposeUiTest {
    show()
    request = NavigationRequest.of("first")
    advance(100)
    assertThat(backStack.toList()).containsExactly(First, Second)

    advance(TRANSITION_MILLIS / 2L)
    // The leaving screen is held at STARTED by the transition, on a focused window.
    assertThat(firstLifecycle?.currentState).isEqualTo(Lifecycle.State.STARTED)
    request = NavigationRequest.of("second")
    advance(TRANSITION_MILLIS.toLong())
    assertThat(backStack.toList()).containsExactly(First, Second)
  }

  @Test
  fun `on a window without focus a request waits, and is performed once the window is focused`() = runComposeUiTest {
    window.registry.currentState = Lifecycle.State.STARTED
    show()
    assertThat(firstLifecycle?.currentState).isEqualTo(Lifecycle.State.STARTED)
    request = NavigationRequest.of("first")
    advance(1_000)
    assertThat(backStack.toList()).containsExactly(First)

    runOnIdle { window.registry.currentState = Lifecycle.State.RESUMED }
    advance(100)
    assertThat(backStack.toList()).containsExactly(First, Second)
  }

  private companion object {
    const val TRANSITION_MILLIS = 1_000
  }
}
