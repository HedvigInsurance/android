package com.hedvig.android.feature.odyssey.step.success

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigCardElevation
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun ClaimSuccessDestination(
  windowSizeClass: WindowSizeClass,
  openChat: () -> Unit,
  navigateBack: () -> Unit,
) {
  ClaimSuccessScreen(
    windowSizeClass = windowSizeClass,
    openChat = openChat,
    navigateBack = navigateBack,
  )
}

@Composable
private fun ClaimSuccessScreen(
  windowSizeClass: WindowSizeClass,
  openChat: () -> Unit,
  navigateBack: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateBack,
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(20.dp))
    HedvigCard(
      shape = RoundedCornerShape(12.dp),
      elevation = HedvigCardElevation.Elevated(),
      modifier = sideSpacingModifier
        .fillMaxWidth(0.66f)
        .wrapContentWidth(Alignment.Start),
    ) {
      Text(
        text = stringResource(R.string.message_claims_record_ok),
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
        style = MaterialTheme.typography.bodyLarge,
      )
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    LargeOutlinedButton(openChat, sideSpacingModifier) {
      Text(stringResource(R.string.open_chat))
    }
    Spacer(Modifier.height(16.dp))
    LargeContainedTextButton(
      onClick = navigateBack,
      text = stringResource(R.string.general_close_button),
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(
      Modifier.windowInsetsPadding(
        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
      ),
    )
  }
}

@HedvigMultiScreenPreview
@Composable
fun PreviewClaimSuccessScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimSuccessScreen(WindowSizeClass.calculateForPreview(), {}, {})
    }
  }
}
