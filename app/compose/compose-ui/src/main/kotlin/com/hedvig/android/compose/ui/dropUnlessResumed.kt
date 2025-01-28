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
