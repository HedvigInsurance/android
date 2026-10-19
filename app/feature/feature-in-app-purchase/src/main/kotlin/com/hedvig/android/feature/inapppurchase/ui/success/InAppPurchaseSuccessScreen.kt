package com.hedvig.android.feature.inapppurchase.ui.success

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.Res
import hedvig.resources.general_close_button
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InAppPurchaseSuccessScreen(startDate: LocalDate?, close: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
  ) {
    Spacer(Modifier.weight(1f))
    EmptyState(
      modifier = Modifier.fillMaxWidth(),
      // TODO: Add "Your insurance is signed" / "Din försäkring är tecknad" to Lokalise
      text = "Your insurance is signed",
      // TODO: Add "It starts on %s" / "Den börjar gälla %s" to Lokalise
      description = startDate?.let { "It starts on $it" },
      iconStyle = SUCCESS,
      buttonStyle = NoButton,
    )
    Spacer(Modifier.weight(1f))
    HedvigTextButton(
      stringResource(Res.string.general_close_button),
      onClick = dropUnlessResumed { close() },
      buttonSize = Large,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewInAppPurchaseSuccessScreen() {
  HedvigTheme {
    Surface {
      InAppPurchaseSuccessScreen(LocalDate(2026, 11, 1), {})
    }
  }
}
