package com.hedvig.android.core.common.android

import android.os.Build

inline fun whenApiVersion(apiVersion: Int, delegate: () -> Unit) {
  if (Build.VERSION.SDK_INT >= apiVersion) {
    delegate()
  }
}
