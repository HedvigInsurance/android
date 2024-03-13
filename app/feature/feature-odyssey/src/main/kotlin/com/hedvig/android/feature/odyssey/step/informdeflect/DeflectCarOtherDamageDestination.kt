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
import androidx.compose.ui.res.stringResource
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
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import hedvig.resources.R

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
    openUrl = {
      val url = deflectCarOtherDamage.partners[0].url
      if (url != null) {
        openUrl(url)
      } else {
        logcat(LogPriority.ERROR) { "Car other damage partner url is null" }
      }
    },
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
    topAppBarText = stringResource(id = R.string.SUBMIT_CLAIM_CAR_TITLE),
  ) {
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(id = R.string.SUBMIT_CLAIM_CAR_REPORT_CLAIM_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = stringResource(id = R.string.SUBMIT_CLAIM_CAR_REPORT_CLAIM_TEXT),
      modifier = Modifier.padding(horizontal = 16.dp),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      onClick = {
        openUrl()
      },
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      Text(
        text = stringResource(id = R.string.SUBMIT_CLAIM_CAR_REPORT_CLAIM_BUTTON),
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
