package com.hedvig.android.feature.travelcertificate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.ConfigureTransparentSystemBars
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.travelcertificate.navigation.GenerateTravelCertificateNavHost

class GenerateTravelCertificateActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      HedvigTheme {
        ConfigureTransparentSystemBars()
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          GenerateTravelCertificateNavHost(
            navController = rememberAnimatedNavController(),
            finish = { finish() },
          )
        }
      }
    }
  }

  companion object {
    fun newInstance(context: Context) = Intent(context, GenerateTravelCertificateActivity::class.java)
  }
}
