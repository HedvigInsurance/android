package com.hedvig.app.ui.compose.composables.screens

import android.content.res.Configuration
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun GenericErrorScreen(
  modifier: Modifier = Modifier,
  title: String = stringResource(id = R.string.home_tab_error_title),
  description: String = stringResource(id = R.string.home_tab_error_body),
  onRetryButtonClicked: () -> Unit,
) {
  GenericInfoScreen(
    modifier = modifier,
    title = title,
    description = description,
    icon = R.drawable.ic_claims,
    primaryButtonText = stringResource(id = R.string.home_tab_error_button_text),
    onPrimaryButtonClicked = onRetryButtonClicked,
    onSecondaryButtonClicked = null,
  )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GenericErrorScreenPreview() {
  HedvigTheme {
    Surface(
      color = MaterialTheme.colors.background,
    ) {
      GenericErrorScreen(onRetryButtonClicked = {})
    }
  }
}
