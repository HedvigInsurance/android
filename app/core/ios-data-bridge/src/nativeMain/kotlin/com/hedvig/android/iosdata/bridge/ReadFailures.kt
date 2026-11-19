package com.hedvig.android.iosdata.bridge

import platform.Foundation.NSError

object ReadFailures {
  /**
   * The failure inside [error], an error a [SessionReads] read delivered, or null when the awaiting task cancelled the
   * read itself.
   */
  fun from(error: NSError): ReadFailure? = error.kotlinThrowable() as? ReadFailure
}
