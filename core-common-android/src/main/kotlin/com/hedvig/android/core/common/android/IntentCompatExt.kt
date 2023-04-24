package com.hedvig.android.core.common.android

import android.content.Intent
import android.os.Parcelable
import androidx.core.content.IntentCompat
import androidx.core.os.BuildCompat
import java.io.Serializable

inline fun <reified T : Parcelable> Intent.parcelableExtra(key: String): T? {
  return IntentCompat.getParcelableExtra(this, key, T::class.java)
}

inline fun <reified T : Parcelable> Intent.parcelableArrayListExtra(key: String): ArrayList<T>? {
  return IntentCompat.getParcelableArrayListExtra(this, key, T::class.java)
}

@BuildCompat.PrereleaseSdkCheck
inline fun <reified T : Serializable> Intent.serializableExtra(key: String): T? {
  return if (BuildCompat.isAtLeastU()) {
    this.getSerializableExtra(key, T::class.java)
  } else {
    @Suppress("DEPRECATION") // https://issuetracker.google.com/issues/242048899
    val serializable = this.getSerializableExtra(key) as? T
    if (T::class.java.isInstance(serializable)) serializable else null
  }
}
