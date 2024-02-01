package com.hedvig.android.tracking.datadog

import com.datadog.android.Datadog
import com.datadog.android.api.SdkCore
import com.datadog.android.rum.GlobalRumMonitor
import com.datadog.android.rum.RumActionType
import com.datadog.android.rum.RumErrorSource
import com.hedvig.android.core.tracking.ActionLogger
import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.ErrorSource

class DatadogActionLogger(
  private val sdkCore: SdkCore,
) : ActionLogger {
  val rumMonitor = GlobalRumMonitor.get(sdkCore)
  override fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>) {
    rumMonitor.addAction(
      when (type) {
        ActionType.TAP -> RumActionType.TAP
        ActionType.SCROLL -> RumActionType.SCROLL
        ActionType.SWIPE -> RumActionType.SWIPE
        ActionType.CLICK -> RumActionType.CLICK
        ActionType.BACK -> RumActionType.BACK
        ActionType.CUSTOM -> RumActionType.CUSTOM
      },
      name,
      attributes,
    )
  }

  override fun logError(
    message: String,
    source: ErrorSource,
    name: String,
    attributes: Map<String, Any?>,
    throwable: Throwable?,
    stacktrace: String?,
  ) {
    val rumErrorSource = when (source) {
      ErrorSource.NETWORK -> RumErrorSource.NETWORK
      ErrorSource.SOURCE -> RumErrorSource.SOURCE
      ErrorSource.LOGGER -> RumErrorSource.LOGGER
    }
    if (stacktrace != null) {
      rumMonitor.addErrorWithStacktrace(
        message = message,
        source = rumErrorSource,
        stacktrace = stacktrace,
        attributes = attributes,
      )
    } else {
      rumMonitor.addError(
        message = message,
        source = rumErrorSource,
        throwable = throwable,
        attributes = attributes,
      )
    }
  }

  companion object {
    fun install(sdkCore: SdkCore = Datadog.getInstance()) {
      if (!ActionLogger.isInstalled) {
        ActionLogger.install(DatadogActionLogger(sdkCore))
      }
    }
  }
}