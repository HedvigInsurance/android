package com.hedvig.android.tracking.datadog

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.initializable.Initializable
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@ContributesIntoSet(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class ActionLoggerInitializer : Initializable {
  override fun initialize() {
    DatadogRumLogger.install()
  }
}
