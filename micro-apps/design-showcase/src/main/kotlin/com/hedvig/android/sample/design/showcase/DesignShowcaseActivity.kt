package com.hedvig.android.sample.design.showcase

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.logger.AndroidLogcatLogger
import com.hedvig.android.tracking.datadog.DatadogRumLogger
import timber.log.Timber

class DesignShowcaseActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge(navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))
    super.onCreate(savedInstanceState)
    Timber.plant(Timber.DebugTree())
    AndroidLogcatLogger.install()
    DatadogRumLogger.install()
    setContent {
      HedvigTheme {
        Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
          DesignShowcase(Modifier.fillMaxSize())
        }
      }
    }
  }
}
