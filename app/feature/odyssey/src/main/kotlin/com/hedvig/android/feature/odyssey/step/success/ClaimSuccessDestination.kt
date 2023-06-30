package com.hedvig.android.feature.odyssey.step.success

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun ClaimSuccessDestination(
  windowSizeClass: WindowSizeClass,
  openChat: () -> Unit,
  navigateUp: () -> Unit,
  closeSuccessScreen: () -> Unit,
) {
  ClaimSuccessScreen(
    windowSizeClass = windowSizeClass,
    openChat = openChat,
    navigateUp = navigateUp,
    closeSuccessScreen = closeSuccessScreen,
  )
}

@Composable
private fun ClaimSuccessScreen(
  windowSizeClass: WindowSizeClass,
  openChat: () -> Unit,
  navigateUp: () -> Unit,
  closeSuccessScreen: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    Box(
      contentAlignment = Alignment.Center,
      modifier = sideSpacingModifier.weight(1f).fillMaxWidth(),
    ) {
      Column(Modifier.fillMaxWidth()) {
        Text(
          text = stringResource(hedvig.resources.R.string.CLAIMS_SUCCESS_TITLE),
          style = MaterialTheme.typography.headlineMedium,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Text(
          text = stringResource(hedvig.resources.R.string.CLAIMS_SUCCESS_LABEL),
          style = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineBreak = LineBreak.Heading,
          ),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
      }
    }
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(R.string.open_chat),
      onClick = openChat,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      onClick = closeSuccessScreen,
      text = stringResource(R.string.general_close_button),
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
fun PreviewClaimSuccessScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimSuccessScreen(WindowSizeClass.calculateForPreview(), {}, {}, {})
    }
  }
}
