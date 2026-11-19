package com.hedvig.android.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Provides [content] with a lifecycle that is RESUMED while the window is visible, whether or not it has focus.
 *
 * Compose desktop resumes a window only while it has focus (STARTED without focus, CREATED while minimised), so
 * everything that waits for RESUMED, such as `dropUnlessResumed` click guards, would wait for a click on a window the
 * member is looking at. A minimised, detached or disposed window keeps the state Compose gives it.
 * Navigation 3 still lowers each entry below this lifecycle during transitions and under overlays.
 */
@Composable
internal fun ProvideVisibleWindowLifecycle(content: @Composable () -> Unit) {
  val window = LocalLifecycleOwner.current.lifecycle
  val owner = remember(window) { VisibleWindowLifecycleOwner(window) }
  DisposableEffect(owner) {
    onDispose { owner.stopFollowing() }
  }
  CompositionLocalProvider(LocalLifecycleOwner provides owner, content = content)
}

private class VisibleWindowLifecycleOwner(private val window: Lifecycle) : LifecycleOwner {
  private val registry = LifecycleRegistry.createUnsafe(this)
  override val lifecycle: Lifecycle get() = registry

  private val observer = LifecycleEventObserver { _, _ ->
    registry.currentState = window.currentState.asVisibleWindowState()
  }

  init {
    window.addObserver(observer)
  }

  fun stopFollowing() {
    window.removeObserver(observer)
  }
}

internal fun Lifecycle.State.asVisibleWindowState(): Lifecycle.State = if (this == Lifecycle.State.STARTED) {
  Lifecycle.State.RESUMED
} else {
  this
}
