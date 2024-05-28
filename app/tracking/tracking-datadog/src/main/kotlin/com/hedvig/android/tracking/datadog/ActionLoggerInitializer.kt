package com.hedvig.android.tracking.datadog

import com.hedvig.android.initializable.Initializable

class ActionLoggerInitializer : Initializable {
  override fun initialize() {
    DatadogRumLogger.install()
  }
}
