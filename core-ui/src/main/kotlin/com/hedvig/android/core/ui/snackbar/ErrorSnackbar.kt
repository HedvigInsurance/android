package com.hedvig.android.core.ui.snackbar

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import hedvig.resources.R

@Composable
fun ErrorSnackbar(hasError: Boolean, showedError: () -> Unit, modifier: Modifier = Modifier) {
  val snackbarHostState = remember { SnackbarHostState() }
  val somethingWentWrongText = stringResource(R.string.something_went_wrong)
  LaunchedEffect(hasError) {
    if (!hasError) return@LaunchedEffect
    snackbarHostState.showSnackbar(somethingWentWrongText)
    showedError()
  }
  SnackbarHost(snackbarHostState, modifier)
}

@Composable
fun ErrorSnackbar(
  errorSnackbarState: ErrorSnackbarState,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val somethingWentWrongText = stringResource(R.string.something_went_wrong)
  LaunchedEffect(errorSnackbarState.error) {
    if (!errorSnackbarState.error) return@LaunchedEffect
    snackbarHostState.showSnackbar(somethingWentWrongText)
    errorSnackbarState.showedError()
  }
  SnackbarHost(snackbarHostState, modifier)
}

class ErrorSnackbarState(
  val error: Boolean,
  val showedError: () -> Unit,
)
