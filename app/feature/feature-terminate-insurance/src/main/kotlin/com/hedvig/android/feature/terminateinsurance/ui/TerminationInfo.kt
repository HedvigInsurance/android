package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.calculateForPreview
import hedvig.resources.R

/**
 * Skeleton for any kind of summary screen about success and failure
 */
@Composable
internal fun TerminationInfoScreen(
  windowSizeClass: WindowSizeClass,
  title: String,
  headerText: String,
  bodyText: String,
  navigateUp: () -> Unit,
  bottomContent: @Composable () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = title,
    navigateUp = navigateUp,
  ) {
    val sideSpacingModifier = if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
      Modifier
        .fillMaxWidth(0.8f)
        .padding(horizontal = 16.dp)
        .align(Alignment.CenterHorizontally)
    } else {
      Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    }
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.weight(1f))
    EmptyState(
      modifier = sideSpacingModifier,
      text = headerText,
      description = bodyText,
      iconStyle = EmptyStateDefaults.EmptyStateIconStyle.ERROR,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.weight(1f))
    Box(sideSpacingModifier) {
      bottomContent()
    }
    Spacer(Modifier.height(16.dp))
    // }
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewTerminationInfoScreen() {
  HedvigTheme {
    Surface {
      TerminationInfoScreen(
        WindowSizeClass.calculateForPreview(),
        title = "",
        headerText = "Cancellation successful",
        bodyText = """
          Your insurance with Hedvig will be cancelled on 31-10-2022. We'll send you a confirmation email with all the details.

          Thanks for being part of Hedvig and trusting us to protect you and your loved ones when needed. The doors are always open if you decide to come back in the near future.
        """.trimIndent(),
        navigateUp = {},
      ) {
        Row(
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth(),
        ) {
          HedvigButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.general_done_button),
            enabled = true,
            onClick = { },
          )
        }
      }
    }
  }
}
