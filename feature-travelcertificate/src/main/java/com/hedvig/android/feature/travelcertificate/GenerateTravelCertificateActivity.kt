package com.hedvig.android.feature.travelcertificate

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.ConfigureTransparentSystemBars
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.navigation.activity.Navigator
import org.koin.android.ext.android.inject

class GenerateTravelCertificateActivity : AppCompatActivity() {
  private val activityNavigator: Navigator by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      HedvigTheme {
        ConfigureTransparentSystemBars()
      }
    }
  }
}
