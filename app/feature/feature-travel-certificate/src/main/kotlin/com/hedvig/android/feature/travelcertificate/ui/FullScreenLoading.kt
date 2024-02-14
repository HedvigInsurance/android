package com.hedvig.android.feature.travelcertificate.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection

@Composable
internal fun FullScreenLoading() {
  Box(modifier = Modifier.fillMaxSize()) {
    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
  }
}

@Composable
internal fun SomethingWrongInfo(onButtonClick: () -> Unit, scope: ColumnScope) { // todo: is it okay to pass a scope like this?
  scope.apply {
    Spacer(Modifier.weight(1f))
    HedvigErrorSection(onButtonClick)
    Spacer(Modifier.weight(1f))
  }
}
