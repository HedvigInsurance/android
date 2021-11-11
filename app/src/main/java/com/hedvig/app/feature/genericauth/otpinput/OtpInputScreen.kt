package com.hedvig.app.feature.genericauth.otpinput

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.systemBarsPadding
import com.hedvig.app.ui.compose.composables.appbar.TopAppBarWithBack
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedButton
import com.hedvig.app.ui.compose.theme.HedvigTheme

@OptIn(ExperimentalUnitApi::class)
@Composable
fun OtpInputScreen(
    onUpClick: () -> Unit,
    onInputChanged: (String) -> Unit,
    onOpenExternalApp: () -> Unit,
    onSubmitCode: (String) -> Unit,
    onResendCode: () -> Unit,
    inputValue: String,
    error: String?,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopAppBarWithBack(
            title = "Log in",
            onClick = onUpClick,
            modifier = Modifier.systemBarsPadding(top = true)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
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
                OutlinedTextField(
                    modifier = Modifier.fillMaxSize(),
                    value = inputValue,
                    onValueChange = {
                        if (it.length <= 6 && it.isDigitsOnly()) {
                            onInputChanged(it)
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
                if (error != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.error),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(144.dp))
            }
            LargeContainedButton(
                onClick = onOpenExternalApp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .navigationBarsWithImePadding(),
            ) {
                Text(text = "Open email app")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OtpInputScreenValidPreview() {
    HedvigTheme {
        OtpInputScreen(
            onUpClick = {},
            onInputChanged = {},
            onResendCode = {},
            onSubmitCode = {},
            onOpenExternalApp = {},
            inputValue = "0123456",
            error = null,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OtpInputScreenInvalidPreview() {
    HedvigTheme {
        OtpInputScreen(
            onUpClick = {},
            onInputChanged = {},
            onResendCode = {},
            onSubmitCode = {},
            onOpenExternalApp = {},
            inputValue = "0123456",
            error = "Code has expired",
        )
    }
}
