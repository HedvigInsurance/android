package com.hedvig.android.sample.design.showcase

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.logger.AndroidLogcatLogger
import com.hedvig.android.sample.design.showcase.ui.MaterialComponents
import com.hedvig.android.tracking.datadog.DatadogRumLogger

class DesignShowcaseActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge(navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))
    super.onCreate(savedInstanceState)
    AndroidLogcatLogger.install()
    DatadogRumLogger.install()
    setContent {
      HedvigTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MaterialComponents(calculateWindowSizeClass(this))
        }
      }
    }
  }
}
