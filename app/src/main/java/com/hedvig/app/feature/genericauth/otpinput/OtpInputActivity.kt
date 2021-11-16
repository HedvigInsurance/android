package com.hedvig.app.feature.genericauth.otpinput

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.insets.systemBarsPadding
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.ui.compose.composables.appbar.TopAppBarWithBack
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.openEmail
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class OtpInputActivity : BaseActivity() {

    val model: OtpInputViewModel by viewModel {
        parametersOf(
            intent.getStringExtra(OTP_ID_EXTRA) ?: throw IllegalArgumentException(
                "Programmer error: Missing OTP_ID in ${this.javaClass.name}"
            ),
            intent.getStringExtra(CREDENTIAL_EXTRA) ?: throw IllegalArgumentException(
                "Programmer error: Missing CREDENTIAL in ${this.javaClass.name}"
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.compatSetDecorFitsSystemWindows(false)

        setContent {
            val scaffoldState = rememberScaffoldState()

            HedvigTheme {

                LaunchedEffect(key1 = Unit) {
                    model.eventsFlow.collectLatest { event ->
                        when (event) {
                            is OtpInputViewModel.Event.Success -> startLoggedIn()
                            OtpInputViewModel.Event.CodeResent -> {
                                delay(1000)
                                val message = getString(R.string.login_snackbar_code_resent)
                                scaffoldState.snackbarHostState.showSnackbar(message)
                            }
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBarWithBack(
                            onClick = ::onBackPressed,
                            title = stringResource(R.string.login_navigation_bar_center_element_title)
                        )
                    },
                    scaffoldState = scaffoldState,
                    modifier = Modifier.systemBarsPadding(top = true),
                ) {
                    val viewState by model.viewState.collectAsState()

                    OtpInputScreen(
                        onInputChanged = model::setInput,
                        onOpenExternalApp = { openEmail(getString(R.string.login_bottom_sheet_view_code)) },
                        onSubmitCode = model::submitCode,
                        onResendCode = model::resendCode,
                        onDismissError = model::dismissError,
                        inputValue = viewState.input,
                        credential = viewState.credential,
                        otpErrorMessage = viewState.otpError?.let(::getString),
                        networkErrorMessage = viewState.networkErrorMessage,
                        loadingResend = viewState.loadingResend,
                        loadingCode = viewState.loadingCode
                    )
                }
            }
        }
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
            credential: String
        ) = Intent(context, OtpInputActivity::class.java).apply {
            putExtra(OTP_ID_EXTRA, id)
            putExtra(CREDENTIAL_EXTRA, credential)
        }
    }
}
