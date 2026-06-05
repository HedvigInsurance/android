package com.hedvig.android.app

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.initializable.Initializable
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class AppInitializers(
  private val initializables: Set<Initializable>,
) {
  fun initialize() {
    for (initializable in initializables) {
      initializable.initialize()
    }
  }
}
