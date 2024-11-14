package com.hedvig.android.sample.design.showcase.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.DialogDefaults
import com.hedvig.android.design.system.hedvig.DialogDefaults.ButtonSize.SMALL
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.BANK_ID
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.Surface

@Composable
fun DialogShowcase() {
  var isDialogVisible by rememberSaveable { mutableStateOf(false) }
  if (isDialogVisible) {
    HedvigDialog(
      onDismissRequest = {
        isDialogVisible = false
      },
      style = DialogDefaults.DialogStyle.Buttons(
        onDismissRequest = {
          isDialogVisible = false
        },
        confirmButtonText = "Continue",
        dismissButtonText = "Cancel",
        onConfirmButtonClick = {
          isDialogVisible = false
        },
        buttonSize = SMALL,
      ),
    ) {
      EmptyState(
        text = "Are you sure?",
        description = "Long description description description description description",
        iconStyle = BANK_ID,
        buttonStyle = NoButton,
      )
    }
  }
  var isErrorDialogVisible by rememberSaveable { mutableStateOf(false) }
  if (isErrorDialogVisible) {
    ErrorDialog(
      onDismiss = { isErrorDialogVisible = false },
      buttonText = "Try again",
      onButtonClick = {},
      title = "There is something wrong",
      message = "Internet connection seems to be lost",
    )
  }

  var isAlertDialogVisible by rememberSaveable { mutableStateOf(false) }
  if (isAlertDialogVisible) {
    HedvigAlertDialog(
      onDismissRequest = { isAlertDialogVisible = false },
      title = "Title",
      text = "Description tralala tralala tralala tralala tralala tralala tralala tralala tralala ",
      onConfirmClick = {},
      confirmButtonLabel = "Confirm",
      dismissButtonLabel = "Dismiss",
    )
  }

  Surface(
    modifier = Modifier
      .fillMaxSize(),
  ) {
    Column(
      modifier = Modifier
        .safeContentPadding()
        .fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        enabled = true,
        onClick = {
          isDialogVisible = true
        },
      ) {
        HedvigText("Open custom info dialog")
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        enabled = true,
        onClick = {
          isErrorDialogVisible = true
        },
      ) {
        HedvigText("Open error dialog")
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        enabled = true,
        onClick = {
          isAlertDialogVisible = true
        },
      ) {
        HedvigText("Open alert dialog")
      }
    }
  }
}
