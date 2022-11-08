package com.hedvig.app.feature.genericauth

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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.app.util.compose.submitOnEnter

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
        style = MaterialTheme.typography.h4,
      )
      Spacer(Modifier.height(40.dp))
      EmailTextField(emailInput, onInputChanged, onSubmitEmail, error, loading, onClear)
      Spacer(Modifier.weight(1f))
      Spacer(Modifier.height(16.dp))
      LargeContainedTextButton(
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
      style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.error),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
  }
}

@Composable
private fun TrailingIcon(
  error: String?,
  loading: Boolean,
  emailInput: String,
  onClear: () -> Unit,
) {
  if (error != null) {
    Image(
      imageVector = Icons.Outlined.ErrorOutline,
      contentDescription = null,
      colorFilter = ColorFilter.tint(color = MaterialTheme.colors.error),
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

@Preview(showBackground = true)
@Composable
fun EmailInputScreenValidPreview() {
  HedvigTheme {
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

@Preview(showBackground = true)
@Composable
fun EmailInputScreenInvalidPreview() {
  HedvigTheme {
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
