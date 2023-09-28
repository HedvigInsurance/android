package com.hedvig.android.feature.demomode

abstract class ProdOrDemoProvider<T>(
  private val demoManager: DemoManager,
  private val demoImpl: T,
  private val prodImpl: T,
) : Provider<T> {

  override fun provide(): T {
    return if (demoManager.isDemoMode) {
      demoImpl
    } else {
      prodImpl
    }
  }
}

