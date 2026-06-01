package com.hedvig.android.design.system.hedvig

import coil3.ImageLoader
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory

/**
 * iOS-only bridge between the Metro dependency graph (built in `shareddi`) and the iOS Compose
 * entry points. `shareddi.initDiGraph` populates these once at startup; ViewControllers read them.
 * Lives here because every iOS ViewController goes through [HedvigComposeUIViewController] and this
 * is the lowest module both `shareddi` and the iOS-facing feature modules already depend on.
 */
object IosDiHolder {
  lateinit var metroViewModelFactory: MetroViewModelFactory
  lateinit var imageLoader: ImageLoader
}
