package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hedvig.resources.Res
import hedvig.resources.something_went_wrong
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorSnackbar(hasError: Boolean, showedError: () -> Unit, modifier: Modifier = Modifier) {
  HedvigSnackbar(
    snackbarText = stringResource(Res.string.something_went_wrong),
    showSnackbar = hasError,
    showedSnackbar = showedError,
    modifier = modifier,
  )
}

@Composable
fun ErrorSnackbar(errorSnackbarState: ErrorSnackbarState, modifier: Modifier = Modifier) {
  HedvigSnackbar(
    snackbarText = errorSnackbarState.messageText ?: stringResource(Res.string.something_went_wrong),
    showSnackbar = errorSnackbarState.error,
    showedSnackbar = errorSnackbarState.showedError,
    modifier = modifier,
    priority = NotificationDefaults.NotificationPriority.Error,
  )
}

class ErrorSnackbarState(
  val error: Boolean,
  val showedError: () -> Unit,
  val messageText: String? = null,
)
