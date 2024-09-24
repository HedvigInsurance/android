package com.hedvig.android.feature.login.genericauth

import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack

@Composable
fun EmailInputScreen(
  onUpClick: () -> Unit,
  onInputChanged: (String) -> Unit,
  onSubmitEmail: () -> Unit,
  emailInput: String,
  error: String?,
  loading: Boolean,
) {
  Column(
    modifier = Modifier
      .safeDrawingPadding()
      .fillMaxSize(),
  ) {
    TopAppBarWithBack(
      onClick = onUpClick,
      title = stringResource(hedvig.resources.R.string.login_navigation_bar_center_element_title),
    )
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState()),
    ) {
      Spacer(Modifier.height(60.dp))
      HedvigText(
        text = stringResource(hedvig.resources.R.string.login_enter_your_email_address),
        style = HedvigTheme.typography.headlineMedium,
      )
      Spacer(Modifier.height(40.dp))
      EmailTextField(emailInput, onInputChanged, onSubmitEmail, error, loading)
      Spacer(Modifier.weight(1f))
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(hedvig.resources.R.string.login_continue_button),
        onClick = onSubmitEmail,
        enabled = true,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun EmailTextField(
  emailInput: String,
  onInputChanged: (String) -> Unit,
  onSubmitEmail: () -> Unit,
  error: String?,
  loading: Boolean,
) {
  HedvigTextField(
    text = emailInput,
    onValueChange = onInputChanged,
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Large,
    labelText = stringResource(hedvig.resources.R.string.login_text_input_email_address),
    suffix = if (loading) {
      { HedvigCircularProgressIndicator() }
    } else {
      null
    },
    errorState = if (error != null) {
      HedvigTextFieldDefaults.ErrorState.Error.WithMessage(error)
    } else {
      HedvigTextFieldDefaults.ErrorState.NoError
    },
    keyboardOptions = KeyboardOptions(
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions(onDone = { onSubmitEmail() }),
    singleLine = true,
    modifier = Modifier
      .fillMaxWidth()
      .submitOnEnter(onSubmitEmail),
  )
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.submitOnEnter(action: () -> Unit) = composed {
  val keyboardController = LocalSoftwareKeyboardController.current
  onKeyEvent { keyEvent ->
    if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
      keyboardController?.hide()
      action()
      true
    } else {
      false
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewEmailInputScreenValid() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      EmailInputScreen(
        onUpClick = {},
        onInputChanged = {},
        onSubmitEmail = {},
        emailInput = "example@example.com",
        error = null,
        loading = false,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewEmailInputScreenInvalid() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      EmailInputScreen(
        onUpClick = {},
        onInputChanged = {},
        onSubmitEmail = {},
        emailInput = "example.com",
        error = "Invalid email",
        loading = false,
      )
    }
  }
}
