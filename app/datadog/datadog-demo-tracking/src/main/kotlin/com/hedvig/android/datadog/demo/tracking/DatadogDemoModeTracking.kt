package com.hedvig.android.datadog.demo.tracking

import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.datadog.core.attributestracking.DatadogAttributesManager
import com.hedvig.android.initializable.Initializable
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.launch

class DatadogDemoModeTracking(
  private val applicationScope: ApplicationScope,
  private val demoManager: DemoManager,
  private val datadogAttributesManager: DatadogAttributesManager,
) : Initializable {
  override fun initialize() {
    applicationScope.launch {
      demoManager.isDemoMode().collect { isDemoMode ->
        logcat(LogPriority.INFO) { "Demo mode changed to:$isDemoMode" }
        datadogAttributesManager.storeAttribute(IS_DEMO_MODE_TRACKING_KEY, isDemoMode)
      }
    }
  }
}

private const val IS_DEMO_MODE_TRACKING_KEY = "is_demo_mode"
