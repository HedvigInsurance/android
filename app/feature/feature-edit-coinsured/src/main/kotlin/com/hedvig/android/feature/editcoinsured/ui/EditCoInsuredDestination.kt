package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack

@Composable
internal fun EditCoInsuredDestination(navigateUp: () -> Unit) {
  EditCoInsuredScreen(navigateUp)
}

@Composable
private fun EditCoInsuredScreen(navigateUp: () -> Unit) {
  Column(Modifier.fillMaxSize()) {
    TopAppBarWithBack(
      title = "Insured people", // TODO
      onClick = navigateUp,
    )
  }
}
