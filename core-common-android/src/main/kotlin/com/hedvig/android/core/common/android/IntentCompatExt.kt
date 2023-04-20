package com.hedvig.android.core.common.android

import android.content.Intent
import android.os.Parcelable
import androidx.core.content.IntentCompat

inline fun <reified T : Parcelable> Intent.parcelableExtra(key: String): T? {
  return IntentCompat.getParcelableExtra(this, key, T::class.java)
}

inline fun <reified T : Parcelable> Intent.parcelableArrayListExtra(key: String): ArrayList<T>? {
  return IntentCompat.getParcelableArrayListExtra(this, key, T::class.java)
}
