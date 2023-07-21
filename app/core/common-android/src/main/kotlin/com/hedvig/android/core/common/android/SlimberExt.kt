package com.hedvig.android.core.common.android

import slimber.log.d
import slimber.log.e
import slimber.log.i

fun d(throwable: Throwable? = null, message: () -> String) {
  if (throwable != null) {
    d(throwable, message)
  } else {
    d(message)
  }
}

fun e(throwable: Throwable? = null, message: () -> String) {
  if (throwable != null) {
    e(throwable, message)
  } else {
    e(message)
  }
}

fun i(throwable: Throwable? = null, message: () -> String) {
  if (throwable != null) {
    i(throwable, message)
  } else {
    i(message)
  }
}
