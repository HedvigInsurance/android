package com.hedvig.android.feature.odyssey.step.informdeflect

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.small.hedvig.ArrowNorthEast
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold

@Composable
internal fun DeflectCarOtherDamageDestination(
  deflectCarOtherDamage: ClaimFlowDestination.DeflectCarOtherDamage,
  closeClaimFlow: () -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
) {
  DeflectCarOtherDamageScreen(
    closeClaimFlow = closeClaimFlow,
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    openUrl = { openUrl(deflectCarOtherDamage.partnerUrl) },
  )
}

@Composable
private fun DeflectCarOtherDamageScreen(
  closeClaimFlow: () -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  openUrl: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    // todo: add real copy here!
    topAppBarText = "Car claim",
  ) {
    Spacer(Modifier.height(8.dp))
    Text(
      // todo: add real copy here!
      text = "Report your claim",
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    Text(
// todo: add real copy here!
      text = "In order to give you the fastest service possible, " +
        "we need to know more about what happened.",
      modifier = Modifier.padding(horizontal = 16.dp),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(24.dp))
    HedvigContainedButton(
      // todo: add real copy here!
      onClick = openUrl,
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      Text(
        // todo: add real copy here!
        text = "Report your claim",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.width(8.dp))
      Icon(
        imageVector = Icons.Hedvig.ArrowNorthEast,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun PreviewDeflectCarOtherDamageScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      DeflectCarOtherDamageScreen(
        {},
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        {},
        {},
      )
    }
  }
}
