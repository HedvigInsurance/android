package com.hedvig.android.feature.odyssey.step.unknownerror

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.Button
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.R

@Composable
internal fun UnknownErrorDestination(
  onNavigateToNewConversation: () -> Unit,
  closeFailureScreenDestination: () -> Unit,
) {
  UnknownErrorScreen(
    onNavigateToNewConversation = onNavigateToNewConversation,
    closeFailureScreenDestination = closeFailureScreenDestination,
  )
}

@Composable
private fun UnknownErrorScreen(onNavigateToNewConversation: () -> Unit, closeFailureScreenDestination: () -> Unit) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
      Spacer(Modifier.height(16.dp))
      EmptyState(
        text = stringResource(R.string.something_went_wrong),
        description = stringResource(R.string.GENERAL_ERROR_BODY),
        iconStyle = ERROR,
        buttonStyle = Button(
          stringResource(R.string.open_chat),
          dropUnlessResumed { onNavigateToNewConversation() },
        ),
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .wrapContentSize(Alignment.Center),
      )
      Spacer(Modifier.height(16.dp))
      HedvigTextButton(
        stringResource(R.string.general_close_button),
        onClick = closeFailureScreenDestination,
        enabled = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewUnknownErrorScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      UnknownErrorScreen({}, {})
    }
  }
}
