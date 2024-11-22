package com.hedvig.android.feature.odyssey.step.unknownscreen

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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.R

@Composable
internal fun UnknownScreenDestination(openPlayStore: () -> Unit, closeUnknownScreenDestination: () -> Unit) {
  UnknownScreenScreen(openPlayStore, closeUnknownScreenDestination)
}

@Composable
private fun UnknownScreenScreen(openPlayStore: () -> Unit, closeUnknownScreenDestination: () -> Unit) {
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
        text = stringResource(R.string.EMBARK_UPDATE_APP_TITLE),
        description = stringResource(R.string.EMBARK_UPDATE_APP_BODY),
        iconStyle = ERROR,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .wrapContentSize(Alignment.Center)
      )
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(R.string.EMBARK_UPDATE_APP_BUTTON),
        onClick = dropUnlessResumed { openPlayStore() },
        enabled = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
      HedvigTextButton(
        text = stringResource(R.string.general_close_button),
        onClick = closeUnknownScreenDestination,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewUnknownScreenScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      UnknownScreenScreen({}, {})
    }
  }
}
