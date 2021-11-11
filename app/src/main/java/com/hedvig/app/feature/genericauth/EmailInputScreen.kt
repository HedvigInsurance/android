package com.hedvig.app.feature.genericauth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.systemBarsPadding
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedButton
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun EmailInputScreen(
    onUpClick: () -> Unit,
    onInputChanged: (String) -> Unit,
    onSubmitEmail: () -> Unit,
    onClear: () -> Unit,
    inputValue: String,
    error: String?,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopAppBar(
            title = { Text(text = "Log in") },
            navigationIcon = {
                IconButton(onClick = onUpClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
            },
            backgroundColor = MaterialTheme.colors.background,
            elevation = 0.dp,
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
                    text = "Please enter your email address below.",
                    style = MaterialTheme.typography.h4,
                )
                Spacer(Modifier.height(40.dp))
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = onInputChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Email address") },
                    trailingIcon = {
                        if (error != null) {
                            Image(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null // TODO: We need a content description here for sure
                            )
                        } else {
                            IconButton(onClick = onClear) {
                                Image(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = null, // TODO: We need a content description here for sure
                                )
                            }
                        }
                    },
                    isError = error != null,
                )
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.error),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Spacer(Modifier.height(144.dp))
            }
            LargeContainedButton(
                onClick = onSubmitEmail,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .navigationBarsWithImePadding(),
            ) {
                Text(text = "Continue")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmailInputScreenValidPreview() {
    HedvigTheme {
        EmailInputScreen(
            onUpClick = {},
            onInputChanged = {},
            onSubmitEmail = {},
            onClear = {},
            inputValue = "johndoe@gmail.com",
            error = null,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmailInputScreenInvalidPreview() {
    HedvigTheme {
        EmailInputScreen(
            onUpClick = {},
            onInputChanged = {},
            onSubmitEmail = {},
            onClear = {},
            inputValue = "johndoe@gmail.com",
            error = "Email address not valid"
        )
    }
}
