package com.hedvig.app.feature.genericauth.otpinput

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.ErrorDialog
import com.hedvig.app.ui.compose.composables.FullScreenProgressOverlay
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedButton
import com.hedvig.app.ui.compose.theme.HedvigTheme

@OptIn(ExperimentalUnitApi::class, ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun OtpInputScreen(
    onInputChanged: (String) -> Unit,
    onOpenExternalApp: () -> Unit,
    onSubmitCode: (String) -> Unit,
    onResendCode: () -> Unit,
    onDismissError: () -> Unit,
    inputValue: String,
    credential: String,
    otpErrorMessage: String?,
    networkErrorMessage: String?,
    loadingResend: Boolean,
    loadingCode: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(60.dp))
            Text(
                text = stringResource(R.string.login_title_check_your_email),
                style = MaterialTheme.typography.h4,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.login_subtitle_verification_code_email, credential),
                style = MaterialTheme.typography.body1,
            )

            Spacer(Modifier.height(40.dp))
            Box(Modifier.fillMaxSize()) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxSize(),
                    value = inputValue,
                    onValueChange = {
                        if (it.length <= 6 && it.isDigitsOnly()) {
                            onInputChanged(it)
                        }

                        if (it.length == 6) {
                            keyboardController?.hide()
                            onSubmitCode(it)
                        }
                    },
                    isError = otpErrorMessage != null,
                    shape = MaterialTheme.shapes.medium,
                    textStyle = LocalTextStyle.current.copy(
                        letterSpacing = TextUnit(20f, TextUnitType.Sp),
                        fontWeight = FontWeight(400),
                        fontSize = TextUnit(28f, TextUnitType.Sp),
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                AnimatedVisibility(visible = otpErrorMessage != null) {
                    Text(
                        text = otpErrorMessage ?: "",
                        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.error),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                onResendCode()
                                keyboardController?.hide()
                            },
                            enabled = !loadingResend
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {

                    val infiniteTransition = rememberInfiniteTransition()
                    val angle by infiniteTransition.animateFloat(
                        initialValue = 0F,
                        targetValue = 360F,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing)
                        )
                    )

                    Icon(
                        modifier = if (loadingResend) Modifier.rotate(angle) else Modifier,
                        painter = painterResource(id = R.drawable.ic_refresh),
                        contentDescription = stringResource(R.string.login_smedium_button_active_resend_code)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = stringResource(R.string.login_smedium_button_active_resend_code),
                        style = MaterialTheme.typography.caption,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(144.dp))
            }
        }
        LargeContainedButton(
            onClick = onOpenExternalApp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Text(text = stringResource(R.string.login_open_email_app_button))
        }
    }

    if (networkErrorMessage != null) {
        ErrorDialog(onDismiss = onDismissError, message = networkErrorMessage)
    }

    FullScreenProgressOverlay(show = loadingCode)
}

@Preview(showBackground = true)
@Composable
fun OtpInputScreenValidPreview() {
    HedvigTheme {
        OtpInputScreen(
            onInputChanged = {},
            onOpenExternalApp = {},
            onSubmitCode = {},
            onResendCode = {},
            onDismissError = {},
            inputValue = "0123456",
            credential = "john@doe.com",
            otpErrorMessage = null,
            networkErrorMessage = null,
            loadingResend = false,
            loadingCode = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OtpInputScreenInvalidPreview() {
    HedvigTheme {
        OtpInputScreen(
            onInputChanged = {},
            onOpenExternalApp = {},
            onSubmitCode = {},
            onResendCode = {},
            onDismissError = {},
            inputValue = "0123456",
            credential = "john@doe.com",
            otpErrorMessage = "Code has expired",
            networkErrorMessage = null,
            loadingResend = false,
            loadingCode = false
        )
    }
}
