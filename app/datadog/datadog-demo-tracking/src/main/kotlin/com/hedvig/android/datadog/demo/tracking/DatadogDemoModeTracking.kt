package com.hedvig.android.datadog.demo.tracking

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.datadog.core.attributestracking.DatadogAttributeProvider
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DatadogDemoModeTracking(
  private val demoManager: DemoManager,
) : DatadogAttributeProvider {
  override fun provide(): Flow<Pair<String, Any?>> {
    return demoManager.isDemoMode().map { isDemoMode ->
      logcat(LogPriority.INFO) { "Demo mode changed to:$isDemoMode" }
      IS_DEMO_MODE_TRACKING_KEY to isDemoMode
    }
  }

  companion object {
    private const val IS_DEMO_MODE_TRACKING_KEY = "is_demo_mode"
  }
}
