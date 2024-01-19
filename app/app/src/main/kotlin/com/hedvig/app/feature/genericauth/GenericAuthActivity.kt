package com.hedvig.app.feature.genericauth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.market.Market
import com.hedvig.app.feature.genericauth.otpinput.OtpInputActivity
import org.koin.androidx.viewmodel.ext.android.getViewModel

class GenericAuthActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    WindowCompat.setDecorFitsSystemWindows(window, false)

    val viewModel: GenericAuthViewModel = getViewModel()
    val market = viewModel.marketManager.market.value

    setContent {
      val viewState by viewModel.viewState.collectAsStateWithLifecycle()

      LaunchedEffect(viewState.verifyUrl) {
        val verifyUrl = viewState.verifyUrl ?: return@LaunchedEffect
        val resendUrl = viewState.resendUrl ?: return@LaunchedEffect
        viewModel.onStartOtpInput()
        startOtpInputActivity(
          verifyUrl = verifyUrl,
          resendUrl = resendUrl,
          email = viewState.emailInputWithoutWhitespaces.value,
        )
      }

      HedvigTheme {
        Surface(
          color = MaterialTheme.colorScheme.background,
          modifier = Modifier.fillMaxSize(),
        ) {
          when (market) {
            Market.SE -> EmailInputScreen(
              onUpClick = ::finish,
              onInputChanged = viewModel::setEmailInput,
              onSubmitEmail = viewModel::submitEmail,
              onClear = viewModel::clear,
              emailInput = viewState.emailInput,
              error = viewState.error?.let { errorMessage(it) },
              loading = viewState.loading,
            )
            Market.NO,
            Market.DK,
            -> SSNInputScreen(
              market = market,
              onUpClick = ::finish,
              onInputChanged = viewModel::setSSNInput,
              onSubmitEmail = viewModel::submitSSN,
              emailInput = viewState.ssnInput,
              error = viewState.error?.let { errorMessage(it) },
              loading = viewState.loading,
            )
          }
        }
      }
    }
  }

  @Composable
  private fun errorMessage(error: GenericAuthViewState.TextFieldError) = stringResource(
    when (error) {
      GenericAuthViewState.TextFieldError.EMPTY ->
        hedvig.resources.R.string.login_text_input_email_error_enter_email

      GenericAuthViewState.TextFieldError.INVALID_EMAIL ->
        hedvig.resources.R.string.login_text_input_email_error_not_valid

      GenericAuthViewState.TextFieldError.NETWORK_ERROR ->
        hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE
    },
  )

  private fun startOtpInputActivity(verifyUrl: String, resendUrl: String, email: String) {
    val intent = OtpInputActivity.newInstance(this, verifyUrl, resendUrl, email)
    startActivity(intent)
  }

  companion object {
    fun newInstance(context: Context) = Intent(context, GenericAuthActivity::class.java)
  }
}
