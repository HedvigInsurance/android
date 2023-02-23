package com.hedvig.android.cancelinsurance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.cancelinsurance.ui.CancelInsuranceScreen
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
class CancelInsuranceActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val insuranceId = intent.getStringExtra(INSURANCE_ID)?.let(::InsuranceId)
      ?: error("Can't open CancelInsuranceActivity without an insurance ID")
    val viewModel = getViewModel<CancelInsuranceViewModel> { parametersOf(insuranceId) }

    setContent {
      HedvigTheme {
        val uiState by viewModel.uiState.collectAsState()
        LaunchedEffect(uiState.dateSubmissionSuccess) {
          if (!uiState.dateSubmissionSuccess) return@LaunchedEffect
          Toast.makeText(this@CancelInsuranceActivity, "Navigate to success screen", Toast.LENGTH_LONG).show()
        }
        CancelInsuranceScreen(
          datePickerState = uiState.datePickerState,
          dateValidator = viewModel.dateValidator,
          canSubmit = uiState.canContinue,
          submit = viewModel::submitSelectedDate,
          hasError = uiState.dateSubmissionError,
          showedError = viewModel::showedError,
          navigateBack = { onBackPressedDispatcher.onBackPressed() },
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
