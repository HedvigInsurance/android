package com.hedvig.app.feature.marketpicker

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hedvig.app.BaseActivity

interface LocaleBroadcastManager {
    fun sendBroadcast()
}

class LocaleBroadcastManagerImpl(private val context: Context) : LocaleBroadcastManager {
    override fun sendBroadcast() {
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent(BaseActivity.LOCALE_BROADCAST))
    }
}
