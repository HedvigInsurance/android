package com.hedvig.android.core.demomode

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * Shared selection logic for a type that has a prod and a demo implementation. Implementors also implement [T] itself
 * and forward each member through [pick] (suspend members) or [pickFlow] (Flow-returning members), so consumers inject
 * the plain [T] and never know demo mode exists.
 */
abstract class DemoSwitcher<T> {
  abstract val demoManager: DemoManager
  abstract val demoImpl: T
  abstract val prodImpl: T

  private fun selected(): Flow<T> =
    demoManager.isDemoMode().distinctUntilChanged().map { isDemo -> if (isDemo) demoImpl else prodImpl }

  protected suspend fun pick(): T = selected().first()

  protected fun <R> pickFlow(block: suspend (T) -> Flow<R>): Flow<R> = selected().flatMapLatest(block)
}
