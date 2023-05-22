package com.hedvig.android.feature.travelcertificate

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.ConfigureTransparentSystemBars
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import com.hedvig.android.feature.travelcertificate.navigation.GenerateTravelCertificateNavHost
import com.hedvig.android.feature.travelcertificate.ui.GenerateTravelCertificateInput
import com.hedvig.android.feature.travelcertificate.ui.mockUiState
import com.hedvig.android.navigation.activity.Navigator
import kotlinx.datetime.LocalDate
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
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          GenerateTravelCertificateNavHost(
            email = null,
            travelCertificateSpecifications = TravelCertificateResult.TravelCertificateSpecifications(
              contractId = "123",
              email = "123@asd.com",
              maxDurationDays = 20,
              dateRange = LocalDate(2023, 5, 23)..LocalDate(2023, 7, 23),
              numberOfCoInsured = 2,
            ),
            windowSizeClass = calculateWindowSizeClass(this@GenerateTravelCertificateActivity),
            navController = rememberAnimatedNavController(),
            finish = { finish() },
          )
        }
      }
    }
  }
}
