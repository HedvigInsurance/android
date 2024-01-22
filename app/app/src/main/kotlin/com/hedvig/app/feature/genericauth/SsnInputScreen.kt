package com.hedvig.app.feature.genericauth

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.market.Market
import hedvig.resources.R

@Composable
fun SSNInputScreen(
  market: Market,
  onUpClick: () -> Unit,
  onInputChanged: (String) -> Unit,
  onSubmitSSN: () -> Unit,
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
      title = stringResource(R.string.zignsec_login_screen_title),
    )
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState()),
    ) {
      Spacer(Modifier.height(60.dp))
      Text(
        text = stringResource(R.string.zignsec_login_screen_title),
        style = MaterialTheme.typography.headlineMedium,
      )
      Spacer(Modifier.height(20.dp))
      SSNTextField(
        input = emailInput,
        market = market,
        onInputChanged = onInputChanged,
        onSubmit = onSubmitSSN,
        error = error,
      )
      Spacer(Modifier.weight(1f))
      Spacer(Modifier.height(16.dp))
      HedvigContainedButton(
        text = stringResource(R.string.login_continue_button),
        onClick = onSubmitSSN,
        isLoading = loading,
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun SSNTextField(
  input: String,
  market: Market,
  onInputChanged: (String) -> Unit,
  onSubmit: () -> Unit,
  error: String?,
) {
  HedvigTextField(
    value = input,
    onValueChange = { newInput ->
      val maxLengthAllowed = when (market) {
        Market.NO -> 11
        Market.DK -> 10
        Market.SE -> error("Should not be able to login with SSN in SE")
      }
      if (newInput.length > maxLengthAllowed) {
        return@HedvigTextField
      }
      onInputChanged(newInput)
    },
    label = {
      Text(
        stringResource(
          when (market) {
            Market.NO -> R.string.simple_sign_login_text_field_label
            Market.DK -> R.string.simple_sign_login_text_field_label_dk
            Market.SE -> error("Should not be able to login with SSN in SE")
          },
        ),
      )
    },
    supportingText = {
      Text(
        stringResource(
          when (market) {
            Market.NO -> R.string.simple_sign_login_text_field_helper_text
            Market.DK -> R.string.simple_sign_login_text_field_helper_text_dk
            Market.SE -> error("Should not be able to login with SSN in SE")
          },
        ),
      )
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Next,
    ),
    keyboardActions = KeyboardActions(
      onNext = { onSubmit() },
    ),
    modifier = Modifier.fillMaxWidth(),
  )
  if (error != null) {
    Text(
      text = error,
      style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewEmailInputScreenValid() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      SSNInputScreen(
        onUpClick = {},
        onInputChanged = {},
        onSubmitSSN = {},
        emailInput = "example@example.com",
        error = null,
        loading = false,
        market = Market.DK,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewEmailInputScreenInvalid() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      SSNInputScreen(
        onUpClick = {},
        onInputChanged = {},
        onSubmitSSN = {},
        emailInput = "example.com",
        error = "Invalid email",
        loading = false,
        market = Market.DK,
      )
    }
  }
}
