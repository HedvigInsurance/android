package com.hedvig.android.core.demomode

import kotlinx.coroutines.flow.first

interface ProdOrDemoProvider<T> : Provider<T> {
  val demoManager: DemoManager
  val demoImpl: T
  val prodImpl: T

  override suspend fun provide(): T {
    return if (demoManager.isDemoMode().first()) {
      demoImpl
    } else {
      prodImpl
    }
  }
}
