package com.hedvig.android.feature.chat.data

import android.content.ContentResolver
import android.content.Intent
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri

internal actual typealias ChatContentResolver = ContentResolver

internal actual fun ChatContentResolver.takePersistableReadPermission(uri: Uri): Throwable? {
  return try {
    takePersistableUriPermission(uri.toAndroidUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION)
    null
  } catch (e: SecurityException) {
    e
  }
}

internal actual fun ChatContentResolver.releasePersistableReadPermission(uri: Uri): Throwable? {
  return try {
    releasePersistableUriPermission(uri.toAndroidUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION)
    null
  } catch (e: SecurityException) {
    e
  }
}
