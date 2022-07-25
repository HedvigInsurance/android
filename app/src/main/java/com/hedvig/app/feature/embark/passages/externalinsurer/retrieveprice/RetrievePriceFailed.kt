package com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.information.AppStateInformationType
import com.hedvig.android.core.ui.genericinfo.GenericInfoScreen
import com.hedvig.app.R

@Composable
fun RetrievePriceFailed(
  onRetry: () -> Unit,
  onSkip: () -> Unit,
  insurerName: String,
) {
  GenericInfoScreen(
    title = stringResource(hedvig.resources.R.string.insurely_failure_title),
    description = stringResource(hedvig.resources.R.string.insurely_failure_description, insurerName),
    informationType = AppStateInformationType.Failure,
    primaryButtonText = stringResource(hedvig.resources.R.string.insurely_failure_retry_button_text),
    onPrimaryButtonClick = { onRetry() },
    secondaryButtonText = stringResource(hedvig.resources.R.string.insurely_failure_skip_button_text),
    onSecondaryButtonClick = { onSkip() },
    modifier = Modifier
      .padding(16.dp)
      .padding(top = 32.dp),
  )
}

@Preview
@Composable
fun RetrievePriceFailedPreview() {
  RetrievePriceFailed({}, {}, "Test insurer")
}
