package com.hedvig.android.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.shared.tier.comparison.data.mockComparisonData
import com.hedvig.android.shared.tier.comparison.ui.ComparisonScreen
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success

class MainActivity : AppCompatActivity() {
  @SuppressLint("NewApi")
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen().apply {
    }
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
    )
    super.onCreate(savedInstanceState)
    setContent {
      com.hedvig.android.design.system.hedvig.HedvigTheme {
        Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
          ComparisonScreen(
            Success(mockComparisonData, 2),
            {},
          )
        }
      }
    }
  }

  companion object {
    fun newInstance(context: Context, withoutHistory: Boolean = false): Intent =
      Intent(context, MainActivity::class.java).apply {
        logcat(LogPriority.INFO) { "MainActivity.newInstance was called. withoutHistory:$withoutHistory" }
        if (withoutHistory) {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
      }
  }
}
