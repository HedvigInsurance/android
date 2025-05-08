package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import hedvig.resources.R

@Composable
fun HedvigErrorSection(
  onButtonClick: () -> Unit,
  modifier: Modifier = Modifier,
  title: String = stringResource(R.string.something_went_wrong),
  subTitle: String? = stringResource(R.string.GENERAL_ERROR_BODY),
  buttonText: String = stringResource(R.string.GENERAL_RETRY),
  windowInsets: WindowInsets = WindowInsets.safeDrawing,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .windowInsetsPadding(windowInsets)
      .padding(horizontal = 16.dp)
      .verticalScroll(rememberScrollState()),
  ) {
    EmptyState(
      text = title,
      description = subTitle,
      iconStyle = ERROR,
      buttonStyle = EmptyStateDefaults.EmptyStateButtonStyle.Button(
        buttonText = buttonText,
        onButtonClick = onButtonClick,
      ),
    )
  }
}


@HedvigShortMultiScreenPreview
@Composable
fun HedvigErrorSectionPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary,
      modifier = Modifier.fillMaxSize()) {
      HedvigErrorSection({})
    }
  }
}
