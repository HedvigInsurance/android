package com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.ui.genericinfo.GenericInfoScreen
import com.hedvig.app.R

@Composable
fun RetrievePriceSuccess(
  onContinue: () -> Unit,
) {
  GenericInfoScreen(
    title = stringResource(hedvig.resources.R.string.insurely_confirmation_title),
    description = stringResource(hedvig.resources.R.string.insurely_confirmation_description),
    onContinue = { onContinue() },
    modifier = Modifier
      .padding(16.dp)
      .padding(top = 32.dp),
  )
}

@Preview
@Composable
fun RetrievePriceSuccessPreview() {
  RetrievePriceSuccess {
  }
}
