package com.hedvig.android.feature.cancelinsurance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.cancelinsurance.ui.CancelInsuranceNavHost

class CancelInsuranceActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val insuranceId = intent.getStringExtra(INSURANCE_ID)?.let(::InsuranceId)
      ?: error("Can't open CancelInsuranceActivity without an insurance ID")

    setContent {
      HedvigTheme {
        CancelInsuranceNavHost(
          windowSizeClass = calculateWindowSizeClass(this),
          navController = rememberNavController(),
          insuranceId = insuranceId,
          navigateUp = ::onSupportNavigateUp,
        )
      }
    }
  }

  companion object {
    private const val INSURANCE_ID = "com.hedvig.android.cancelinsurance.INSURANCE_ID"

    fun newInstance(context: Context, insuranceId: String): Intent {
      return Intent(context, CancelInsuranceActivity::class.java)
        .putExtra(INSURANCE_ID, insuranceId)
    }
  }
}
