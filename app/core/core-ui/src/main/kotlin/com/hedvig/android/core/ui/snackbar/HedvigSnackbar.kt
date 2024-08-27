package com.hedvig.android.core.ui.snackbar

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun HedvigSnackbar(
  snackbarText: String,
  showSnackbar: Boolean,
  showedSnackbar: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(showSnackbar, snackbarText) {
    if (!showSnackbar) return@LaunchedEffect
    showedSnackbar()
  }
  SnackbarHost(snackbarHostState, modifier)
}
