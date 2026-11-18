package com.hedvig.android.feature.greencard.ui.overview

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.feature.greencard.data.GreenCardUrl
import hedvig.resources.GENERAL_DOWNLOAD
import hedvig.resources.GENERAL_SHOW
import hedvig.resources.Res
import hedvig.resources.general_done_button
import java.io.File
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GreenCardOverviewDestination(
  greenCardUrl: GreenCardUrl,
  viewModel: GreenCardOverviewViewModel,
  navigateUp: () -> Unit,
  onDone: () -> Unit,
  onShareGreenCard: (File) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  GreenCardOverviewScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onDone = onDone,
    onDownloadGreenCard = { viewModel.emit(GreenCardOverviewEvent.DownloadGreenCard(greenCardUrl)) },
    onShareGreenCard = onShareGreenCard,
  )
}

@Composable
private fun GreenCardOverviewScreen(
  uiState: GreenCardOverviewUiState,
  navigateUp: () -> Unit,
  onDone: () -> Unit,
  onDownloadGreenCard: () -> Unit,
  onShareGreenCard: (File) -> Unit,
) {
  when (uiState) {
    GreenCardOverviewUiState.Loading -> {
      HedvigFullScreenCenterAlignedProgress(Modifier.fillMaxSize())
    }

    GreenCardOverviewUiState.Failure -> {
      HedvigScaffold(navigateUp = navigateUp, topAppBarActionType = TopAppBarActionType.CLOSE) {
        HedvigErrorSection(onButtonClick = onDownloadGreenCard, modifier = Modifier.weight(1f))
      }
    }

    is GreenCardOverviewUiState.Success -> {
      val downloadedGreenCard = uiState.downloadedGreenCard
      LaunchedEffect(downloadedGreenCard) {
        downloadedGreenCard?.let { onShareGreenCard(File(it.path)) }
      }
      HedvigScaffold(navigateUp = navigateUp, itemsColumnHorizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.weight(1f))
        EmptyState(
          // TODO: Add "Your green card is ready" / "Ditt gröna kort är klart" to Lokalise
          text = "Your green card is ready",
          // TODO: Add "We will send the physical copy to the address you entered." / "Vi skickar det fysiska kortet till adressen du angav." to Lokalise
          description = "We will send the physical copy to the address you entered.",
          iconStyle = SUCCESS,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        HedvigNotificationCard(
          // TODO: Add "We recommend that you print the green card and keep it in the vehicle." / "Vi rekommenderar att du skriver ut det gröna kortet och har det i fordonet." to Lokalise
          message = "We recommend that you print the green card and keep it in the vehicle.",
          priority = NotificationPriority.Info,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = if (downloadedGreenCard != null) {
            // TODO: Add "Show green card" / "Visa grönt kort" to Lokalise
            "Show green card"
          } else {
            // TODO: Add "Download green card" / "Ladda ner grönt kort" to Lokalise
            "Download green card"
          },
          onClick = {
            if (downloadedGreenCard != null) {
              onShareGreenCard(File(downloadedGreenCard.path))
            } else {
              onDownloadGreenCard()
            }
          },
          onClickLabel = if (downloadedGreenCard != null) {
            stringResource(Res.string.GENERAL_SHOW)
          } else {
            stringResource(Res.string.GENERAL_DOWNLOAD)
          },
          enabled = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
        HedvigTextButton(
          text = stringResource(Res.string.general_done_button),
          onClick = onDone,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewGreenCardOverview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      GreenCardOverviewScreen(
        uiState = GreenCardOverviewUiState.Success(null),
        navigateUp = {},
        onDone = {},
        onDownloadGreenCard = {},
        onShareGreenCard = {},
      )
    }
  }
}
