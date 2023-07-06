package com.hedvig.android.feature.odyssey.step.unknownerror

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.information.AppStateInformation
import com.hedvig.android.core.designsystem.component.information.AppStateInformationType
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import hedvig.resources.R

@Composable
internal fun UnknownErrorDestination(
  openChat: () -> Unit,
  closeFailureScreenDestination: () -> Unit,
) {
  UnknownErrorScreen(
    openChat = openChat,
    closeFailureScreenDestination = closeFailureScreenDestination,
  )
}

@Composable
private fun UnknownErrorScreen(
  openChat: () -> Unit,
  closeFailureScreenDestination: () -> Unit,
) {
  HedvigTheme(useNewColorScheme = true) {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier.fillMaxSize(),
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        Spacer(Modifier.height(16.dp))
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.padding(horizontal = 16.dp).weight(1f),
        ) {
          AppStateInformation(
            type = AppStateInformationType.Failure,
            title = stringResource(R.string.home_tab_error_title),
            description = stringResource(R.string.home_tab_error_body),
            horizontalAlignment = Alignment.CenterHorizontally,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).fillMaxWidth(0.8f),
          )
        }
        Spacer(Modifier.height(16.dp))
        HedvigContainedButton(
          text = stringResource(R.string.open_chat),
          onClick = openChat,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        HedvigTextButton(
          stringResource(R.string.general_close_button),
          closeFailureScreenDestination,
          Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewUnknownErrorScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      UnknownErrorScreen({}, {})
    }
  }
}
