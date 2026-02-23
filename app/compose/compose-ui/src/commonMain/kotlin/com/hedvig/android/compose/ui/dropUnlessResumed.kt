package com.hedvig.android.compose.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * A copy of [androidx.lifecycle.compose.dropUnlessResumed] which allows for one generic [T] parameter
 */
@Composable
fun <T> dropUnlessResumed(block: (T) -> Unit): (T) -> Unit {
  return with(LocalLifecycleOwner.current) {
    { t ->
      if (lifecycle.currentState.isAtLeast(State.RESUMED)) {
        block(t)
      }
    }
  }
}

/**
 * A copy of [androidx.lifecycle.compose.dropUnlessResumed] which allows for two generic [E], [T] parameters
 */
@Composable
fun <E, T> dropUnlessResumed(block: (E, T) -> Unit): (E, T) -> Unit {
  return with(LocalLifecycleOwner.current) {
    { e, t ->
      if (lifecycle.currentState.isAtLeast(State.RESUMED)) {
        block(e, t)
      }
    }
  }
}
