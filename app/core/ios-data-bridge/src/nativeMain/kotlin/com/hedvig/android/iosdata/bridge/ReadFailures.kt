package com.hedvig.android.iosdata.bridge

import platform.Foundation.NSError

object ReadFailures {
  /** The [ReadFailure] inside [error], an error a read delivered to Swift, or null when it holds something else. */
  fun from(error: NSError): ReadFailure? = error.userInfo["KotlinException"] as? ReadFailure
}
