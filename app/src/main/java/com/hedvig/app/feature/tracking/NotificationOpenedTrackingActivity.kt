package com.hedvig.app.feature.tracking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.hanalytics.HAnalytics
import org.koin.android.ext.android.inject

class NotificationOpenedTrackingActivity : AppCompatActivity() {
  private val hAnalytics: HAnalytics by inject()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    intent.getStringExtra(NOTIFICATION_TYPE)?.let { hAnalytics.notificationOpened(it) }
    finish()
  }

  companion object {
    private const val NOTIFICATION_TYPE = "NOTIFICATION_TYPE"
    fun newInstance(context: Context, type: String) = Intent(
      context,
      NotificationOpenedTrackingActivity::class.java,
    ).apply {
      putExtra(NOTIFICATION_TYPE, type)
    }
  }
}
