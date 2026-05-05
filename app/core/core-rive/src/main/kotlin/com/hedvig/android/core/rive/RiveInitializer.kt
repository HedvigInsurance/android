package com.hedvig.android.core.rive

import android.content.Context
import app.rive.runtime.kotlin.core.Rive
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

object RiveInitializer {
  private var isInitialized = false

  fun init(context: Context) {
    try {
      Rive.init(context)
      isInitialized = true
      logcat(LogPriority.INFO) { "Rive initialized successfully" }
    } catch (e: UnsatisfiedLinkError) {
      logcat(LogPriority.ERROR, e) { "Failed to initialize Rive (native library not found): ${e.message}" }
    } catch (e: Exception) {
      logcat(LogPriority.ERROR, e) { "Unexpected error initializing Rive: ${e.message}" }
    }
  }

  fun isAvailable(): Boolean = isInitialized
}
