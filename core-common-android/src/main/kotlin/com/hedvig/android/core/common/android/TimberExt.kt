package com.hedvig.android.core.common.android

import timber.log.Timber

inline fun <T> ifPlanted(action: () -> T): T? {
  return if (Timber.treeCount != 0) {
    action()
  } else {
    null
  }
}
