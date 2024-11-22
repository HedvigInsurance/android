package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import hedvig.resources.R

@Composable
internal fun RemoveCoInsuredBottomSheetContent(
  onDismiss: () -> Unit,
  onRemove: (CoInsured) -> Unit,
  isLoading: Boolean,
  errorMessage: String?, //todo: this one is not used?
  coInsured: CoInsured,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(Modifier.height(16.dp))
    HedvigText(stringResource(id = R.string.CONTRACT_REMOVE_COINSURED_CONFIRMATION))
    Spacer(Modifier.height(24.dp))
    HedvigButton(
      text = stringResource(id = R.string.REMOVE_CONFIRMATION_BUTTON),
      onClick = {
        onRemove(coInsured)
      },
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Red,
      isLoading = isLoading,
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      onClick = onDismiss,
      text = stringResource(R.string.general_cancel_button),
      enabled = true,
      buttonStyle = Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
@HedvigPreview
private fun RemoveCoInsuredBottomSheetContentPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      RemoveCoInsuredBottomSheetContent(
        onDismiss = {},
        onRemove = {},
        isLoading = false,
        coInsured = CoInsured(
          "Tester",
          "Testersson",
          birthDate = null,
          ssn = "144412022193",
          hasMissingInfo = false,
        ),
        errorMessage = null,
      )
    }
  }
}

@Composable
@HedvigPreview
private fun RemoveCoInsuredBottomSheetContentWithCoInsuredPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      RemoveCoInsuredBottomSheetContent(
        onDismiss = {},
        onRemove = {},
        isLoading = false,
        coInsured = CoInsured(
          "Tester",
          "Testersson",
          birthDate = null,
          ssn = "144412022193",
          hasMissingInfo = false,
        ),
        errorMessage = null,
      )
    }
  }
}
