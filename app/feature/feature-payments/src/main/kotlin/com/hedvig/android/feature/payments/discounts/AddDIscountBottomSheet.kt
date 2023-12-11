package com.hedvig.android.feature.payments.discounts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import hedvig.resources.R

@Composable
internal fun AddDiscountBottomSheet(isLoading: Boolean, errorMessage: String?, onAddDiscount: (String) -> Unit) {
  var discountCodeInput by remember { mutableStateOf("") }
  Column(
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(id = R.string.PAYMENTS_ADD_CAMPAIGN_CODE),
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    HedvigTextField(
      value = discountCodeInput,
      errorText = errorMessage,
      onValueChange = {
        discountCodeInput = it
      },
      label = {
        Text(text = stringResource(id = R.string.REFERRAL_ADDCOUPON_INPUTPLACEHOLDER))
      },
      withNewDesign = true,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(id = R.string.PAYMENTS_ADD_CODE_BUTTON_LABEL),
      enabled = discountCodeInput.isNotBlank(),
      onClick = {
        onAddDiscount(discountCodeInput)
      },
      isLoading = isLoading,
    )
    Spacer(Modifier.height(32.dp))
  }
}

@Composable
@HedvigPreview
private fun AddDiscountBottomSheetPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AddDiscountBottomSheet(
        isLoading = false,
        errorMessage = null,
        onAddDiscount = {},
      )
    }
  }
}
