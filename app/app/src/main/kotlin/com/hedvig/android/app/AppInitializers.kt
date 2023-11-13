package com.hedvig.android.app

import com.hedvig.android.initializable.Initializable

class AppInitializers(
  private val initializables: Set<Initializable>,
) {
  fun initialize() {
    for (initializable in initializables) {
      initializable.initialize()
    }
  }
}
