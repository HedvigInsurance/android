package com.hedvig.android.network.clients

import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import io.ktor.client.plugins.logging.Logger

class HedvigHttpLogger : Logger {
  override fun log(message: String) {
    // todo do we need to filter content disposition?
    if (message.contains("Content-Disposition")) {
      logcat(LogPriority.VERBOSE, tag = "Ktor") { "File upload omitted from log" }
    } else if (message.contains("Content-Type: image/jpeg")) {
      logcat(LogPriority.VERBOSE, tag = "Ktor") { "File download omitted from log" }
    } else {
      logcat(LogPriority.VERBOSE, tag = "Ktor") { message }
    }
  }
}
