package com.hedvig.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

inline fun newBroadcastReceiver(crossinline closure: (context: Context?, intent: Intent?) -> Unit):
    BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        closure(context, intent)
    }
}
