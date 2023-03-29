package com.hedvig.android.feature.terminateinsurance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.tryOpenPlayStore
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceNavHost
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
    val insuranceDisplayName = intent.getStringExtra(INSURANCE_DISPLAY_NAME)
      ?: error("Can't open TerminateInsuranceActivity without an insurance display name")

    setContent {
      HedvigTheme {
        Box(Modifier.fillMaxSize(), propagateMinConstraints = true) {
          TerminateInsuranceNavHost(
            windowSizeClass = calculateWindowSizeClass(this@TerminateInsuranceActivity),
            navController = rememberAnimatedNavController(),
            insuranceId = insuranceId,
            insuranceDisplayName = insuranceDisplayName,
            openChat = {
              onSupportNavigateUp()
              activityNavigator.navigateToChat(this@TerminateInsuranceActivity)
            },
            openPlayStore = { tryOpenPlayStore() },
            navigateUp = { onSupportNavigateUp() },
            finishTerminationFlow = { finish() },
          )
        }
      }
    }
  }

  companion object {
    private const val INSURANCE_ID = "com.hedvig.android.feature.terminateinsurance.INSURANCE_ID"
    private const val INSURANCE_DISPLAY_NAME = "com.hedvig.android.feature.terminateinsurance.INSURANCE_DISPLAY_NAME"

    fun newInstance(context: Context, insuranceId: String, insuranceDisplayName: String): Intent {
      return Intent(context, TerminateInsuranceActivity::class.java)
        .putExtra(INSURANCE_ID, insuranceId)
        .putExtra(INSURANCE_DISPLAY_NAME, insuranceDisplayName)
    }
  }
}
