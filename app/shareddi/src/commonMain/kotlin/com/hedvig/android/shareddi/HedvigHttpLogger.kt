package com.hedvig.android.shareddi

import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import io.ktor.client.plugins.logging.Logger

class HedvigHttpLogger : Logger {
  override fun log(message: String) {
    // todo do we need to filter content disposition?
//    if (message.contains("Content-Disposition")) { ... }
//    "File upload omitted from log"
    logcat(LogPriority.VERBOSE, tag = "Ktor") { message }
  }
}
