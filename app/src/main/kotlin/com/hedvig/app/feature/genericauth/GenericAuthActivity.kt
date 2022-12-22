package com.hedvig.app.feature.genericauth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.feature.genericauth.otpinput.OtpInputActivity
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import org.koin.androidx.viewmodel.ext.android.getViewModel

class GenericAuthActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.compatSetDecorFitsSystemWindows(false)

    val viewModel: GenericAuthViewModel = getViewModel()
    setContent {
      val viewState by viewModel.viewState.collectAsState()

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
        EmailInputScreen(
          onUpClick = ::finish,
          onInputChanged = viewModel::setInput,
          onSubmitEmail = viewModel::submitEmail,
          onClear = viewModel::clear,
          emailInput = viewState.emailInput,
          error = viewState.error?.let { errorMessage(it) },
          loading = viewState.loading,
        )
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
