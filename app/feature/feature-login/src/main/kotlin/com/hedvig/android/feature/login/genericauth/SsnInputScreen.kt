package com.hedvig.android.feature.login.genericauth

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
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
  canSubmitSsn: Boolean,
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
      HedvigText(
        text = stringResource(R.string.zignsec_login_screen_title),
        style = HedvigTheme.typography.headlineMedium,
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
      HedvigButton(
        text = stringResource(R.string.login_continue_button),
        onClick = onSubmitSSN,
        isLoading = loading,
        enabled = canSubmitSsn,
        modifier = Modifier.fillMaxWidth(),
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
    text = input,
    onValueChange = onValueChange@{ newInput ->
      if (!newInput.isDigitsOnly()) return@onValueChange
      val maxLengthAllowed = when (market) {
        Market.NO -> 11
        Market.DK -> 10
        Market.SE -> error("Should not be able to login with SSN in SE")
      }
      if (newInput.length > maxLengthAllowed) {
        return@onValueChange
      }
      onInputChanged(newInput)
    },
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
    labelText = stringResource(
      when (market) {
        Market.NO -> R.string.simple_sign_login_text_field_label
        Market.DK -> R.string.simple_sign_login_text_field_label_dk
        Market.SE -> error("Should not be able to login with SSN in SE")
      },
    ),
    supportingText = {
      HedvigText(
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
    HedvigText(
      text = error,
      style = HedvigTheme.typography.bodySmall.copy(color = HedvigTheme.colorScheme.signalRedElement),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewEmailInputScreenValid() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SSNInputScreen(
        onUpClick = {},
        onInputChanged = {},
        onSubmitSSN = {},
        emailInput = "example@example.com",
        error = null,
        loading = false,
        market = Market.DK,
        canSubmitSsn = true,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewEmailInputScreenInvalid() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SSNInputScreen(
        onUpClick = {},
        onInputChanged = {},
        onSubmitSSN = {},
        emailInput = "example.com",
        error = "Invalid email",
        loading = false,
        market = Market.DK,
        canSubmitSsn = true,
      )
    }
  }
}
