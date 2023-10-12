package com.hedvig.android.sample.design.showcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.hedvig.android.core.designsystem.theme.ConfigureTransparentSystemBars
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.logger.AndroidLogcatLogger
import com.hedvig.android.sample.design.showcase.ui.MaterialComponents

class DesignShowcaseActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    AndroidLogcatLogger.install()
    setContent {
      HedvigTheme {
        ConfigureTransparentSystemBars()
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MaterialComponents(calculateWindowSizeClass(this))
        }
      }
    }
  }
}
