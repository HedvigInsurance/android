package com.feature.changeaddress

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.feature.changeaddress.navigation.ChangeAddressNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme

class ChangeAddressActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      HedvigTheme {
        ChangeAddressNavHost(
          windowSizeClass = calculateWindowSizeClass(this@ChangeAddressActivity),
          navController = rememberAnimatedNavController(),
          navigateUp = { onSupportNavigateUp() },
          finish = { finish() },
        )
      }
    }
  }
}
