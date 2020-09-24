package com.hedvig.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hedvig.app.feature.settings.Language
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity {
    constructor() : super()
    constructor(@LayoutRes layout: Int) : super(layout)

    open val preventRecreation = false

    private val tracker: ScreenTracker by inject()

    override fun onResume() {
        tracker.screenView(javaClass.simpleName)
        super.onResume()
    }

    override fun onDestroy() {
        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(localeListener)
        super.onDestroy()
    }

    private var localeListener = LocaleListener()

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(Language.fromSettings(newBase)?.apply(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(localeListener, IntentFilter().apply { addAction(LOCALE_BROADCAST) })
        Language.fromSettings(this)?.apply(this)
    }

    companion object {
        const val LOCALE_BROADCAST = "LOCALE_BROADCAST"
    }

    inner class LocaleListener : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!preventRecreation) recreate()
        }
    }
}

