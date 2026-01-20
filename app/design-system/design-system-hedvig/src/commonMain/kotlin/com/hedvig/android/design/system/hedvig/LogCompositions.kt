package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.hedvig.android.logger.logcat

/**
 * Note the inline function below which ensures that this function is essentially
 * copied at the call site to ensure that its logging only recompositions from the
 * original call site.
 */
@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun LogCompositions(message: String) {
  val ref = remember { Ref(0) }
  SideEffect { ref.value++ }
  logcat { "Debug Log Compositions: $message ${ref.value}" }
}

/**
 * Only for use with [LogCompositions], not defined as private because inline [LogCompositions] doesn't allow it.
 */
class Ref(var value: Int)
