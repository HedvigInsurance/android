package com.hedvig.android.app.notification

import android.app.PendingIntent
import android.os.Build

fun getImmutablePendingIntentFlags(): Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

fun getMutablePendingIntentFlags(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
  PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
} else {
  PendingIntent.FLAG_UPDATE_CURRENT
}

const val DATA_MESSAGE_TITLE = "DATA_MESSAGE_TITLE"
const val DATA_MESSAGE_BODY = "DATA_MESSAGE_BODY"
