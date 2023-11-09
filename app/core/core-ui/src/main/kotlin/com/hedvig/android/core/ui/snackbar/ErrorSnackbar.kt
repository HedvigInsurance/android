package com.hedvig.android.core.ui.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import hedvig.resources.R

@Composable
fun ErrorSnackbar(hasError: Boolean, showedError: () -> Unit, modifier: Modifier = Modifier) {
  HedvigSnackbar(
    snackbarText = stringResource(R.string.something_went_wrong),
    showSnackbar = hasError,
    showedSnackbar = showedError,
    modifier = modifier,
  )
}

@Composable
fun ErrorSnackbar(errorSnackbarState: ErrorSnackbarState, modifier: Modifier = Modifier) {
  HedvigSnackbar(
    snackbarText = stringResource(R.string.something_went_wrong),
    showSnackbar = errorSnackbarState.error,
    showedSnackbar = errorSnackbarState.showedError,
    modifier = modifier,
  )
}

class ErrorSnackbarState(
  val error: Boolean,
  val showedError: () -> Unit,
)
