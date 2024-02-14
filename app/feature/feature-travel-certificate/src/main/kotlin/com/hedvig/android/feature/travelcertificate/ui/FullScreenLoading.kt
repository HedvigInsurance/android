package com.hedvig.android.feature.travelcertificate.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hedvig.android.core.designsystem.component.information.HedvigInformationSection
import hedvig.resources.R

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
    HedvigInformationSection(
      title = stringResource(id = R.string.something_went_wrong),
      buttonText = stringResource(id = R.string.GENERAL_RETRY),
      onButtonClick = onButtonClick,
    )
    Spacer(Modifier.weight(1f))
  }
}
