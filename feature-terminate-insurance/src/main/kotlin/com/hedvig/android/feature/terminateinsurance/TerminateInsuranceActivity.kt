package com.hedvig.android.feature.terminateinsurance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.view.WindowCompat
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.terminateinsurance.ui.TerminateInsuranceNavHost
import com.hedvig.android.navigation.activity.Navigator
import org.koin.android.ext.android.inject

class TerminateInsuranceActivity : AppCompatActivity() {

  private val activityNavigator: Navigator by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val insuranceId = intent.getStringExtra(INSURANCE_ID)?.let(::InsuranceId)
      ?: error("Can't open TerminateInsuranceActivity without an insurance ID")

    setContent {
      HedvigTheme {
        TerminateInsuranceNavHost(
          windowSizeClass = calculateWindowSizeClass(this),
          navController = rememberAnimatedNavController(),
          insuranceId = insuranceId,
          openChat = {
            onSupportNavigateUp()
            activityNavigator.navigateToChat(this)
          },
          navigateUp = { onSupportNavigateUp() },
          finishTerminationFlow = { finish() },
        )
      }
    }
  }

  companion object {
    private const val INSURANCE_ID = "com.hedvig.android.feature.terminateinsurance.INSURANCE_ID"

    fun newInstance(context: Context, insuranceId: String): Intent {
      return Intent(context, TerminateInsuranceActivity::class.java)
        .putExtra(INSURANCE_ID, insuranceId)
    }
  }
}
