package com.hedvig.android.odyssey.step.unknownscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.information.AppStateInformationType
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.genericinfo.GenericInfoScreen
import hedvig.resources.R

@Composable
fun UnknownScreenDestination(
  openChat: () -> Unit,
  navigateBack: () -> Unit,
) {
  UnknownScreenScreen(openChat, navigateBack)
}

@Composable
private fun UnknownScreenScreen( // todo maybe show "Update your app" here instead.
  openChat: () -> Unit,
  navigateBack: () -> Unit,
) {
  Column {
    TopAppBarWithBack(
      onClick = navigateBack,
      title = "",
    )
    Spacer(Modifier.height(20.dp))
    GenericInfoScreen(
      title = stringResource(R.string.something_went_wrong),
      description = stringResource(R.string.home_tab_error_body),
      informationType = AppStateInformationType.Failure,
      primaryButtonText = stringResource(R.string.general_done_button),
      onPrimaryButtonClick = navigateBack,
      secondaryButtonText = stringResource(R.string.open_chat),
      onSecondaryButtonClick = openChat,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(
      Modifier.windowInsetsPadding(
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
      ),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewUnknownScreenScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      UnknownScreenScreen({}, {})
    }
  }
}
