package com.hedvig.android.feature.greencard.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.design.system.hedvig.rememberHedvigMonthDateTimeFormatter
import com.hedvig.android.feature.greencard.data.GreenCard
import hedvig.resources.Res
import hedvig.resources.general_error
import hedvig.resources.travel_certificate_active
import hedvig.resources.travel_certificate_downloading_error
import hedvig.resources.travel_certificate_expired
import java.io.File
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GreenCardHistoryDestination(
  viewModel: GreenCardHistoryViewModel,
  navigateUp: () -> Unit,
  onStartGenerateGreenCardFlow: (contractId: String) -> Unit,
  onNavigateToChooseContract: () -> Unit,
  onShareGreenCard: (File) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  GreenCardHistoryScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    reload = { viewModel.emit(GreenCardHistoryEvent.RetryLoadData) },
    onGreenCardClick = { viewModel.emit(GreenCardHistoryEvent.DownloadGreenCard(it)) },
    onDismissDownloadError = { viewModel.emit(GreenCardHistoryEvent.DismissDownloadError) },
    onStartGenerateGreenCardFlow = onStartGenerateGreenCardFlow,
    onNavigateToChooseContract = onNavigateToChooseContract,
    onShareGreenCard = onShareGreenCard,
  )
}

@Composable
private fun GreenCardHistoryScreen(
  uiState: GreenCardHistoryUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  onGreenCardClick: (signedUrl: String) -> Unit,
  onDismissDownloadError: () -> Unit,
  onStartGenerateGreenCardFlow: (contractId: String) -> Unit,
  onNavigateToChooseContract: () -> Unit,
  onShareGreenCard: (File) -> Unit,
) {
  when (uiState) {
    GreenCardHistoryUiState.Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    GreenCardHistoryUiState.Failure -> {
      HedvigScaffold(navigateUp = navigateUp, topAppBarText = GREEN_CARD_TITLE) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    is GreenCardHistoryUiState.Success -> {
      val downloadedGreenCard = uiState.downloadedGreenCard
      if (downloadedGreenCard != null) {
        LaunchedEffect(downloadedGreenCard) {
          onShareGreenCard(File(downloadedGreenCard.path))
        }
      }
      if (uiState.showDownloadError) {
        ErrorDialog(
          title = stringResource(Res.string.general_error),
          message = stringResource(Res.string.travel_certificate_downloading_error),
          onDismiss = onDismissDownloadError,
        )
      }
      if (uiState.isDownloadingGreenCard) {
        HedvigFullScreenCenterAlignedProgress()
      } else {
        GreenCardHistorySuccessScreen(
          uiState = uiState,
          navigateUp = navigateUp,
          onGreenCardClick = onGreenCardClick,
          onGenerateGreenCard = {
            val singleContractId = uiState.eligibleContractIds.singleOrNull()
            if (singleContractId != null) {
              onStartGenerateGreenCardFlow(singleContractId)
            } else {
              onNavigateToChooseContract()
            }
          },
        )
      }
    }
  }
}

@Composable
private fun GreenCardHistorySuccessScreen(
  uiState: GreenCardHistoryUiState.Success,
  navigateUp: () -> Unit,
  onGreenCardClick: (signedUrl: String) -> Unit,
  onGenerateGreenCard: () -> Unit,
) {
  HedvigScaffold(navigateUp = navigateUp, topAppBarText = GREEN_CARD_TITLE) {
    Box(Modifier.weight(1f)) {
      Column(
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
      ) {
        if (uiState.greenCards.isEmpty()) {
          Spacer(Modifier.weight(1f))
          // TODO: Add "To view your green cards, you must first generate one." / "För att se dina gröna kort måste du först skapa ett." to Lokalise
          EmptyState(
            text = "To view your green cards, you must first generate one.",
            description = null,
            iconStyle = INFO,
            modifier = Modifier.fillMaxWidth(),
          )
        } else {
          GreenCardsList(uiState.greenCards, onGreenCardClick)
        }
        Spacer(Modifier.weight(1f))
        if (uiState.eligibleContractIds.isNotEmpty()) {
          Spacer(Modifier.height(8.dp))
          HedvigButton(
            // TODO: Add "Get green card" / "Skapa grönt kort" to Lokalise
            text = "Get green card",
            onClick = dropUnlessResumed { onGenerateGreenCard() },
            buttonStyle = Secondary,
            enabled = true,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
        }
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun GreenCardsList(greenCards: List<GreenCard>, onGreenCardClick: (signedUrl: String) -> Unit) {
  val dateTimeFormatter = rememberHedvigMonthDateTimeFormatter()
  Spacer(Modifier.height(16.dp))
  greenCards.groupBy { it.validFrom.year }.forEach { (year, greenCardsOfYear) ->
    HedvigText(text = year.toString(), modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(4.dp))
    greenCardsOfYear.forEachIndexed { index, greenCard ->
      val color = if (greenCard.isExpiredNow) HedvigTheme.colorScheme.signalRedElement else Color.Unspecified
      HorizontalItemsWithMaximumSpaceTaken(
        spaceBetween = 8.dp,
        startSlot = {
          Column {
            HedvigText(text = greenCard.registrationNumber, color = color)
            HedvigText(
              text = "${dateTimeFormatter.format(
                greenCard.validFrom,
              )} – ${dateTimeFormatter.format(greenCard.validTo)}",
              color = HedvigTheme.colorScheme.textSecondary,
            )
          }
        },
        endSlot = {
          HedvigText(
            text = if (greenCard.isExpiredNow) {
              stringResource(Res.string.travel_certificate_expired)
            } else {
              stringResource(Res.string.travel_certificate_active)
            },
            color = color,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
          )
        },
        modifier = Modifier
          .horizontalDivider(DividerPosition.Top, show = index != 0, horizontalPadding = 16.dp)
          .clickable { onGreenCardClick(greenCard.signedUrl) }
          .padding(16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

// TODO: Add "Green card" / "Grönt kort" to Lokalise
internal const val GREEN_CARD_TITLE = "Green card"

@HedvigShortMultiScreenPreview
@Composable
private fun PreviewGreenCardHistoryScreen(
  @PreviewParameter(GreenCardHistoryUiStateProvider::class) uiState: GreenCardHistoryUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      GreenCardHistoryScreen(uiState, {}, {}, {}, {}, {}, {}, {})
    }
  }
}

private class GreenCardHistoryUiStateProvider :
  CollectionPreviewParameterProvider<GreenCardHistoryUiState>(
    listOf(
      GreenCardHistoryUiState.Success(
        greenCards = listOf(
          GreenCard("1", "", "ABC 123", LocalDate(2026, 12, 20), LocalDate(2027, 1, 6), isExpiredNow = false),
          GreenCard("2", "", "XYZ 789", LocalDate(2026, 6, 1), LocalDate(2026, 6, 30), isExpiredNow = true),
          GreenCard("3", "", "ABC 123", LocalDate(2025, 7, 1), LocalDate(2025, 7, 20), isExpiredNow = true),
        ),
        eligibleContractIds = listOf("a", "b"),
        isDownloadingGreenCard = false,
        showDownloadError = false,
        downloadedGreenCard = null,
      ),
      GreenCardHistoryUiState.Success(
        greenCards = emptyList(),
        eligibleContractIds = listOf("a"),
        isDownloadingGreenCard = false,
        showDownloadError = false,
        downloadedGreenCard = null,
      ),
      GreenCardHistoryUiState.Failure,
    ),
  )
