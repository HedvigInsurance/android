package com.hedvig.app.feature.marketpicker

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hedvig.app.BaseActivity

class LocaleBroadcastManager(private val context: Context) {
  fun sendBroadcast(recreate: Boolean = false) {
    LocalBroadcastManager
      .getInstance(context)
      .sendBroadcast(Intent(BaseActivity.LOCALE_BROADCAST).also { it.putExtra(RECREATE, recreate) })
  }

  companion object {
    const val RECREATE = "RECREATE"
  }
}
