package com.hedvig.android.feature.payments.ui.discounts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.R

@Composable
internal fun AddDiscountBottomSheetContent(isLoading: Boolean, errorMessage: String?, onAddDiscount: (String) -> Unit) {
  var discountCodeInput by remember { mutableStateOf("") }
  Column(
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = stringResource(id = R.string.PAYMENTS_ADD_CAMPAIGN_CODE),
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    HedvigTextField(
      text = discountCodeInput,
      errorState = if (errorMessage !=
        null
      ) {
        HedvigTextFieldDefaults.ErrorState.Error.WithMessage(errorMessage)
      } else {
        HedvigTextFieldDefaults.ErrorState.NoError
      },
      textFieldSize = TextFieldSize.Medium,
      onValueChange = {
        discountCodeInput = it
      },
      labelText = stringResource(id = R.string.REFERRAL_ADDCOUPON_INPUTPLACEHOLDER),
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(id = R.string.PAYMENTS_ADD_CODE_BUTTON_LABEL),
      enabled = discountCodeInput.isNotBlank(),
      modifier = Modifier.fillMaxWidth(),
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
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AddDiscountBottomSheetContent(
        isLoading = false,
        errorMessage = null,
        onAddDiscount = {},
      )
    }
  }
}
