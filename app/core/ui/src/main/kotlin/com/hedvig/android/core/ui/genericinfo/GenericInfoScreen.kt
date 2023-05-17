package com.hedvig.android.core.ui.genericinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedButton
import com.hedvig.android.core.designsystem.component.information.AppStateInformation
import com.hedvig.android.core.designsystem.component.information.AppStateInformationType
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

/**
 * https://www.figma.com/file/tpp00CvD8ALUKdjDRzyygv/Android%E2%80%A8-UI-Kit?node-id=3126%3A5146
 */

@Composable
fun GenericInfoScreen(
  title: String,
  description: String,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  GenericInfoScreen(
    title = title,
    description = description,
    informationType = AppStateInformationType.Information,
    buttonText = stringResource(hedvig.resources.R.string.general_continue_button),
    onButtonClick = onContinue,
    modifier = modifier,
  )
}

@Composable
fun GenericInfoScreen(
  title: String,
  description: String,
  informationType: AppStateInformationType,
  buttonText: String,
  onButtonClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  GenericInfoScreen(
    title = title,
    description = description,
    informationType = informationType,
    primaryButtonText = buttonText,
    onPrimaryButtonClick = onButtonClick,
    secondaryButtonText = null,
    onSecondaryButtonClick = null,
    modifier = modifier,
  )
}

@Composable
fun GenericInfoScreen(
  title: String,
  description: String,
  informationType: AppStateInformationType,
  primaryButtonText: String,
  onPrimaryButtonClick: () -> Unit,
  secondaryButtonText: String?,
  onSecondaryButtonClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    AppStateInformation(
      type = informationType,
      title = title,
      description = description,
    )
    Spacer(Modifier.height(40.dp))
    Column(
      modifier = Modifier.fillMaxHeight(),
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
    ) {
      LargeContainedButton(onClick = onPrimaryButtonClick) {
        Text(text = primaryButtonText)
      }
      if (secondaryButtonText != null && onSecondaryButtonClick != null) {
        LargeOutlinedButton(onClick = onSecondaryButtonClick) {
          Text(text = secondaryButtonText)
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewGenericInfoScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      GenericInfoScreen(
        title = "Title",
        description = "description test test test",
        informationType = AppStateInformationType.Success,
        primaryButtonText = "Continue",
        onPrimaryButtonClick = {},
        secondaryButtonText = "Retry",
        {},
      )
    }
  }
}
