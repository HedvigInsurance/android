package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import hedvig.resources.R

@Composable
internal fun RemoveCoInsuredBottomSheetContent(
  onDismiss: () -> Unit,
  onRemove: (CoInsured) -> Unit,
  isLoading: Boolean,
  errorMessage: String?,
  coInsured: CoInsured,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(Modifier.height(16.dp))
    Text(stringResource(id = R.string.CONTRACT_REMOVE_COINSURED_CONFIRMATION))
    Spacer(Modifier.height(24.dp))
    HedvigContainedButton(
      text = stringResource(id = R.string.REMOVE_CONFIRMATION_BUTTON),
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
        disabledContainerColor = MaterialTheme.colorScheme.error.copy(
          alpha = 0.12f,
        ),
        disabledContentColor = MaterialTheme.colorScheme.onError.copy(
          alpha = 0.38f,
        ),
      ),
      onClick = {
        onRemove(coInsured)
      },
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
    Surface(color = MaterialTheme.colorScheme.background) {
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
    Surface(color = MaterialTheme.colorScheme.background) {
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
