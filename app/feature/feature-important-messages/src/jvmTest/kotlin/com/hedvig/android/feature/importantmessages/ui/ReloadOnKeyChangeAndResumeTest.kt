package com.hedvig.android.feature.importantmessages.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

/** The presenter loads once when it starts, so these count only the reloads asked for on top of that load. */
@OptIn(ExperimentalTestApi::class)
class ReloadOnKeyChangeAndResumeTest {
  private val lifecycleOwner = TestLifecycleOwner()
  private var reloadKey: Any? by mutableStateOf("en")
  private var reloads = 0

  @Test
  fun `the first composition and the first resume do not reload`() = runComposeUiTest {
    show()

    assertThat(reloads).isEqualTo(0)
  }

  @Test
  fun `a new reloadKey reloads once`() = runComposeUiTest {
    show()

    runOnIdle { reloadKey = "sv" }
    waitForIdle()

    assertThat(reloads).isEqualTo(1)
  }

  @Test
  fun `every resume after the first reloads`() = runComposeUiTest {
    show()

    setLifecycleState(Lifecycle.State.CREATED)
    setLifecycleState(Lifecycle.State.RESUMED)
    setLifecycleState(Lifecycle.State.STARTED)
    setLifecycleState(Lifecycle.State.RESUMED)

    assertThat(reloads).isEqualTo(2)
  }

  private fun ComposeUiTest.show() {
    setContent {
      CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
        ReloadOnKeyChangeAndResume(reloadKey = reloadKey, reload = { reloads++ })
      }
    }
    waitForIdle()
  }

  private fun ComposeUiTest.setLifecycleState(state: Lifecycle.State) {
    runOnIdle { lifecycleOwner.registry.currentState = state }
    waitForIdle()
  }
}

private class TestLifecycleOwner : LifecycleOwner {
  val registry: LifecycleRegistry = LifecycleRegistry.createUnsafe(this).apply {
    currentState = Lifecycle.State.RESUMED
  }
  override val lifecycle: Lifecycle
    get() = registry
}
