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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.accompanist.insets.systemBarsPadding
import com.hedvig.app.BaseActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.ui.compose.composables.ErrorDialog
import com.hedvig.app.ui.compose.composables.appbar.TopAppBarWithBack
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.openEmail
import kotlinx.coroutines.launch
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
                Scaffold(
                    topBar = {
                        TopAppBarWithBack(
                            onClick = ::onBackPressed,
                            title = "Log in"
                        )
                    },
                    scaffoldState = scaffoldState,
                    modifier = Modifier.systemBarsPadding(top = true),
                ) {
                    val viewState by model.viewState.collectAsState()
                    val events = model.eventsFlow.collectAsState(initial = null)
                    val openDialog = remember { mutableStateOf(false) }

                    LaunchedEffect(events.value) {
                        when (events.value) {
                            is OtpInputViewModel.Event.Success -> startLoggedIn()
                            OtpInputViewModel.Event.CodeResent -> launch {
                                scaffoldState.snackbarHostState.showSnackbar("Code resent")
                            }
                            OtpInputViewModel.Event.None -> return@LaunchedEffect
                            OtpInputViewModel.Event.ShowDialog -> openDialog.value = true
                        }
                    }

                    if (openDialog.value) {
                        ErrorDialog(show = openDialog, message = viewState.errorMessage)
                    }

                    OtpInputScreen(
                        onInputChanged = model::setInput,
                        onOpenExternalApp = { openEmail("View code") },
                        onSubmitCode = model::submitCode,
                        onResendCode = model::resendCode,
                        inputValue = viewState.input,
                        otpErrorMessage = viewState.errorEvent?.getErrorResource()?.let(::getString),
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
