package com.hedvig.app.util.extensions

import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

inline fun <reified T> RecyclerView.ViewHolder.invalid(_data: T) =
  logcat(LogPriority.ERROR) {
    "Invalid data passed to ${this.javaClass.name}::bind - type is ${T::class.java.name}, value is $_data"
  }
