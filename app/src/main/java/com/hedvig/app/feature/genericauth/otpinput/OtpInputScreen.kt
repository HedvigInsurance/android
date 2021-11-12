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
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
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
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedButton
import com.hedvig.app.ui.compose.theme.HedvigTheme

@OptIn(ExperimentalUnitApi::class, ExperimentalAnimationApi::class)
@Composable
fun OtpInputScreen(
    onInputChanged: (String) -> Unit,
    onOpenExternalApp: () -> Unit,
    onSubmitCode: (String) -> Unit,
    onResendCode: () -> Unit,
    inputValue: String,
    error: String?,
    loadingResend: Boolean,
    loadingCode: Boolean
) {
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
                text = "Check your email",
                style = MaterialTheme.typography.h4,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Click the log in button in the email or enter the " +
                    "6-digit code we've sent to johndoe@gmail.com.",
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
                            onSubmitCode(it)
                        }
                    },
                    isError = error != null,
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

                if (loadingCode) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                            .align(Alignment.CenterEnd)
                            .absoluteOffset(x = (-20).dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                AnimatedVisibility(visible = error != null) {
                    Text(
                        text = error ?: "",
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
                        .clickable(onClick = onResendCode)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
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
                        contentDescription = "Resend code"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = "Resend code",
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
            Text(text = "Open email app")
        }
    }
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
            inputValue = "0123456",
            error = null,
            loadingResend = false,
            loadingCode = true,
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
            inputValue = "0123456",
            error = "Code has expired",
            loadingResend = false,
            loadingCode = false,
        )
    }
}
