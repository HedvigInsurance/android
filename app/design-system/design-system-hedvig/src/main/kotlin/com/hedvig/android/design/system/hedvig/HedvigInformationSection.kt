package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import hedvig.resources.R

@Composable
fun HedvigInformationSection(
  title: String,
  modifier: Modifier = Modifier,
  subTitle: String? = null,
  onButtonClick: (() -> Unit)? = null,
  buttonText: String  = stringResource(id = R.string.ALERT_OK),
  windowInsets: WindowInsets = WindowInsets.safeDrawing,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .windowInsetsPadding(windowInsets)
      .padding(horizontal = 16.dp),
  ) {
    val buttonStyle = if (onButtonClick!=null) EmptyStateDefaults.EmptyStateButtonStyle.Button(
      buttonText = buttonText,
      onButtonClick = onButtonClick,
    ) else NoButton
    EmptyState(
      text = title,
      description = subTitle,
      iconStyle =  EmptyStateDefaults.EmptyStateIconStyle.INFO,
      buttonStyle = buttonStyle,
    )
  }
}
