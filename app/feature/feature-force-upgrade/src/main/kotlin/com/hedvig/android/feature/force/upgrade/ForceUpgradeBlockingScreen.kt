package com.hedvig.android.feature.force.upgrade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.R

@Composable
fun ForceUpgradeBlockingScreen(goToPlayStore: () -> Unit) {
  UpgradeApp(goToPlayStore)
}

@Composable
private fun UpgradeApp(goToPlayStore: () -> Unit) {
  Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxSize()
      .wrapContentWidth(Alignment.CenterHorizontally)
      .padding(16.dp)
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .widthIn(max = 400.dp),
  ) {
    HedvigText(
      text = stringResource(R.string.EMBARK_UPDATE_APP_TITLE),
      style = HedvigTheme.typography.headlineLarge,
    )
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = stringResource(R.string.EMBARK_UPDATE_APP_BODY),
      textAlign = TextAlign.Center,
      style = HedvigTheme.typography.bodySmall.copy(
        lineBreak = LineBreak.Heading,
      ),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.EMBARK_UPDATE_APP_BUTTON),
      onClick = dropUnlessResumed { goToPlayStore() },
      enabled = true,
      buttonSize = Medium,
    )
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewUpgradeApp() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      UpgradeApp(goToPlayStore = {})
    }
  }
}
