package com.hedvig.app.feature.genericauth

import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack

@Composable
fun EmailInputScreen(
  onUpClick: () -> Unit,
  onInputChanged: (String) -> Unit,
  onSubmitEmail: () -> Unit,
  onClear: () -> Unit,
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
      Text(
        text = stringResource(hedvig.resources.R.string.login_enter_your_email_address),
        style = MaterialTheme.typography.headlineMedium,
      )
      Spacer(Modifier.height(40.dp))
      EmailTextField(emailInput, onInputChanged, onSubmitEmail, error, loading, onClear)
      Spacer(Modifier.weight(1f))
      Spacer(Modifier.height(16.dp))
      HedvigContainedButton(
        text = stringResource(hedvig.resources.R.string.login_continue_button),
        onClick = onSubmitEmail,
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
  onClear: () -> Unit,
) {
  OutlinedTextField(
    value = emailInput,
    onValueChange = onInputChanged,
    modifier = Modifier
      .fillMaxWidth()
      .submitOnEnter(onSubmitEmail),
    label = { Text(stringResource(hedvig.resources.R.string.login_text_input_email_address)) },
    trailingIcon = {
      TrailingIcon(error, loading, emailInput, onClear)
    },
    isError = error != null,
    keyboardOptions = KeyboardOptions(
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions(onDone = { onSubmitEmail() }),
    singleLine = true,
  )
  if (error != null) {
    Text(
      text = error,
      style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
  }
}

@Composable
private fun TrailingIcon(error: String?, loading: Boolean, emailInput: String, onClear: () -> Unit) {
  if (error != null) {
    Image(
      imageVector = Icons.Outlined.ErrorOutline,
      contentDescription = null,
      colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.error),
    )
  } else if (loading) {
    CircularProgressIndicator(modifier = Modifier.size(24.dp))
  } else if (emailInput.isNotBlank()) {
    IconButton(onClick = onClear) {
      Image(
        imageVector = Icons.Filled.Clear,
        contentDescription = stringResource(
          hedvig.resources.R.string.login_text_input_email_address_icon_description_clear_all,
        ),
      )
    }
  }
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
    Surface(color = MaterialTheme.colorScheme.background) {
      EmailInputScreen(
        onUpClick = {},
        onInputChanged = {},
        onSubmitEmail = {},
        onClear = {},
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
    Surface(color = MaterialTheme.colorScheme.background) {
      EmailInputScreen(
        onUpClick = {},
        onInputChanged = {},
        onSubmitEmail = {},
        onClear = {},
        emailInput = "example.com",
        error = "Invalid email",
        loading = false,
      )
    }
  }
}
