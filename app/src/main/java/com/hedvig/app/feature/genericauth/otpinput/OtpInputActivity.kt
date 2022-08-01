package com.hedvig.app.feature.genericauth.otpinput

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.insets.systemBarsPadding
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.openEmail
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.seconds

class OtpInputActivity : BaseActivity() {

  val model: OtpInputViewModel by viewModel {
    parametersOf(
      intent.getStringExtra(OTP_ID_EXTRA) ?: throw IllegalArgumentException(
        "Programmer error: Missing OTP_ID in ${this.javaClass.name}",
      ),
      intent.getStringExtra(CREDENTIAL_EXTRA) ?: throw IllegalArgumentException(
        "Programmer error: Missing CREDENTIAL in ${this.javaClass.name}",
      ),
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.compatSetDecorFitsSystemWindows(false)

    setContent {
      val scaffoldState = rememberScaffoldState()

      HedvigTheme {
        LaunchedEffect(Unit) {
          model.events.collectLatest { event ->
            when (event) {
              is OtpInputViewModel.Event.Success -> startLoggedIn()
              OtpInputViewModel.Event.CodeResent -> {
                delay(1.seconds)
                val message = getString(hedvig.resources.R.string.login_snackbar_code_resent)
                scaffoldState.snackbarHostState.showSnackbar(message)
              }
            }
          }
        }

        Scaffold(
          topBar = {
            TopAppBarWithBack(
              onClick = ::onBackPressed,
              title = stringResource(hedvig.resources.R.string.login_navigation_bar_center_element_title),
            )
          },
          scaffoldState = scaffoldState,
          modifier = Modifier.systemBarsPadding(top = true),
        ) { paddingValues ->
          val viewState by model.viewState.collectAsState()

          OtpInputScreen(
            onInputChanged = model::setInput,
            onOpenExternalApp = { openEmail(getString(hedvig.resources.R.string.login_bottom_sheet_view_code)) },
            onSubmitCode = model::submitCode,
            onResendCode = model::resendCode,
            onDismissError = model::dismissError,
            inputValue = viewState.input,
            credential = viewState.credential,
            otpErrorMessage = viewState.otpError?.toStringRes()?.let(::getString),
            networkErrorMessage = viewState.networkErrorMessage,
            loadingResend = viewState.loadingResend,
            loadingCode = viewState.loadingCode,
            modifier = Modifier.padding(paddingValues),
          )
        }
      }
    }
  }

  private fun OtpResult.Error.OtpError.toStringRes() = when (this) {
    OtpResult.Error.OtpError.AlreadyCompleted -> hedvig.resources.R.string.login_code_input_error_msg_code_already_used
    OtpResult.Error.OtpError.Expired -> hedvig.resources.R.string.login_code_input_error_msg_expired
    OtpResult.Error.OtpError.TooManyAttempts -> {
      hedvig.resources.R.string.login_code_input_error_msg_too_many_wrong_attempts
    }
    OtpResult.Error.OtpError.Unknown -> hedvig.resources.R.string.general_unknown_error
    OtpResult.Error.OtpError.WrongOtp -> hedvig.resources.R.string.login_code_input_error_msg_code_not_valid
  }

  private fun startLoggedIn() {
    val intent = LoggedInActivity.newInstance(this, withoutHistory = true)
    startActivity(intent)
  }

  companion object {
    private const val OTP_ID_EXTRA = "OTP_ID_EXTRA"
    private const val CREDENTIAL_EXTRA = "CREDENTIAL_EXTRA"

    fun newInstance(
      context: Context,
      id: String,
      credential: String,
    ) = Intent(context, OtpInputActivity::class.java).apply {
      putExtra(OTP_ID_EXTRA, id)
      putExtra(CREDENTIAL_EXTRA, credential)
    }
  }
}
