package com.hedvig.android.core.common.android

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BuildCompat
import androidx.core.os.BundleCompat
import java.io.Serializable

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? {
  return BundleCompat.getParcelable(this, key, T::class.java)
}

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? {
  return BundleCompat.getParcelableArrayList(this, key, T::class.java)
}

@BuildCompat.PrereleaseSdkCheck
inline fun <reified T : Serializable> Bundle.serializable(key: String): T? {
  return if (BuildCompat.isAtLeastU()) {
    this.getSerializable(key, T::class.java)
  } else {
    @Suppress("DEPRECATION") // https://issuetracker.google.com/issues/242048899
    val parcelable = this.getSerializable(key) as? T
    if (T::class.java.isInstance(parcelable)) parcelable else null
  }
}
