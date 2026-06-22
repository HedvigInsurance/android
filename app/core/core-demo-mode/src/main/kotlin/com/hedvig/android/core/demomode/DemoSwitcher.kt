package com.hedvig.android.core.demomode

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Shared selection logic for a type that has a prod and a demo implementation. Implementors also implement [T] itself
 * and forward each member through [pick] (suspend members) or [pickFlow] (Flow-returning members), so consumers inject
 * the plain [T] and never know demo mode exists.
 */
abstract class DemoSwitcher<T> {
  abstract val demoManager: DemoManager
  abstract val demoImpl: T
  abstract val prodImpl: T

  protected suspend fun pick(): T {
    return if (demoManager.isDemoMode().first()) demoImpl else prodImpl
  }

  protected fun <R> pickFlow(block: suspend (T) -> Flow<R>): Flow<R> {
    return demoManager.isDemoMode().distinctUntilChanged().flatMapLatest { isDemo ->
      block(if (isDemo) demoImpl else prodImpl)
    }
  }
}
