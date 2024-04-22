package com.hedvig.android.feature.force.upgrade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
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
      .padding(16.dp),
  ) {
    Text(
      text = stringResource(R.string.EMBARK_UPDATE_APP_TITLE),
      style = MaterialTheme.typography.headlineMedium,
    )
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(R.string.EMBARK_UPDATE_APP_BODY),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodyLarge.copy(
        lineBreak = LineBreak.Heading,
      ),
    )
    Spacer(Modifier.height(16.dp))
    HedvigContainedSmallButton(
      text = stringResource(R.string.EMBARK_UPDATE_APP_BUTTON),
      onClick = goToPlayStore,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewUpgradeApp() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      UpgradeApp(goToPlayStore = {})
    }
  }
}
