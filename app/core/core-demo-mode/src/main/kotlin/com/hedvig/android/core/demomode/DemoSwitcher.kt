package com.hedvig.android.core.demomode

import kotlinx.coroutines.flow.first

/**
 * Shared selection logic for a type that has a prod and a demo implementation. Implementors also implement [T] itself
 * and forward each member through [pick], so consumers inject the plain [T] and never know demo mode exists.
 */
interface DemoSwitcher<T> {
  val demoManager: DemoManager
  val demoImpl: T
  val prodImpl: T

  suspend fun pick(): T = if (demoManager.isDemoMode().first()) demoImpl else prodImpl
}
