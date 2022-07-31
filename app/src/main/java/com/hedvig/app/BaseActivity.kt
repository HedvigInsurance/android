package com.hedvig.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hedvig.android.market.Language
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.marketpicker.LocaleBroadcastManager
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity {
  constructor() : super()
  constructor(@LayoutRes layout: Int) : super(layout)

  open val preventRecreation = false
  open val screenName: String = javaClass.simpleName

  private val marketManager: MarketManager by inject()

  override fun onDestroy() {
    LocalBroadcastManager
      .getInstance(this)
      .unregisterReceiver(localeListener)
    super.onDestroy()
  }

  private var localeListener = LocaleListener()

  override fun attachBaseContext(newBase: Context?) {
    if (newBase != null) {
      super.attachBaseContext(Language.fromSettings(newBase, marketManager.market).apply(newBase))
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    LocalBroadcastManager
      .getInstance(this)
      .registerReceiver(localeListener, IntentFilter().apply { addAction(LOCALE_BROADCAST) })
    Language.fromSettings(this, marketManager.market).apply(this)
  }

  companion object {
    const val LOCALE_BROADCAST = "LOCALE_BROADCAST"
  }

  inner class LocaleListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (!preventRecreation) {
        if (intent?.getBooleanExtra(LocaleBroadcastManager.RECREATE, false) == true) {
          viewModelStore.clear()
        }
        recreate()
      }
    }
  }
}
