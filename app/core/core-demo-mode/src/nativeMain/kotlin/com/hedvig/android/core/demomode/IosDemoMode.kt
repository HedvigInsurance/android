package com.hedvig.android.core.demomode

/**
 * The iOS app's demo mode flag, which the iOS app owns. Kotlin calls both members on the main thread only.
 */
interface IosDemoMode {
  fun isDemoMode(): Boolean

  fun setDemoMode(demoMode: Boolean)
}
