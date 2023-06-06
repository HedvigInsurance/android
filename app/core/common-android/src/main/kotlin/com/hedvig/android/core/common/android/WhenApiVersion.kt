package com.hedvig.android.core.common.android

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
inline fun whenApiVersion(apiVersion: Int, delegate: () -> Unit) {
  if (Build.VERSION.SDK_INT >= apiVersion) {
    delegate()
  }
}
