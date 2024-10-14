package com.hedvig.android.feature.change.tier.ui.stepsummary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigThreeDotsProgressIndicator
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.change.tier.ui.stepcustomize.PillAndBasicInfo
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Failure
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Loading
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.MakingChanges
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Success
import hedvig.resources.R
import java.io.File

@Composable
internal fun ChangeTierSummaryDestination(
  viewModel: SummaryViewModel,
  navigateUp: () -> Unit,
  onSuccess: () -> Unit,
  onFailure: () -> Unit,
  sharePdf: (File) -> Unit,
) {
  val uiState: SummaryState by viewModel.uiState.collectAsStateWithLifecycle()
  when (uiState) {
    Failure -> HedvigScaffold(navigateUp) {
      HedvigErrorSection(
        onButtonClick = {
          viewModel.emit(SummaryEvent.Reload)
        },
      )
    }

    Loading -> HedvigFullScreenCenterAlignedProgress()

    is MakingChanges -> {
      val state = uiState as MakingChanges
      LaunchedEffect(state.navigateToSuccess) {
        val success = state.navigateToSuccess
        if (success) {
          viewModel.emit(SummaryEvent.ClearNavigation)
          onSuccess()
        }
      }
      MakingChangesScreen()
    }

    is Success -> {
      val state = uiState as Success
      LaunchedEffect(state.navigateToFail) {
        val fail = state.navigateToFail
        if (fail) {
          viewModel.emit(SummaryEvent.ClearNavigation)
          onFailure()
        }
      }

      SummarySuccessScreen(
        uiState = state,
        navigateUp = navigateUp,
        onConfirmClick = { viewModel.emit(SummaryEvent.SubmitQuote) },
        downloadFromUrl = { url -> viewModel.emit(SummaryEvent.DownLoadFromUrl(url)) },
        sharePdf = { file ->
          viewModel.emit(SummaryEvent.HandledSharingPdfFile)
          sharePdf(file)
        },
      )
    }
  }
}

@Composable
private fun MakingChangesScreen() {
  HedvigFullScreenCenterAlignedLinearProgress(
    title =
      stringResource(R.string.TIER_FLOW_COMMIT_PROCESSING_LOADING_TITLE),
  )
}

@Composable
private fun SummarySuccessScreen(
  uiState: SummaryState.Success,
  onConfirmClick: () -> Unit,
  downloadFromUrl: (String) -> Unit,
  sharePdf: (File) -> Unit,
  navigateUp: () -> Unit,
) {
  if (uiState.savedFileUri != null) {
    LaunchedEffect(uiState.savedFileUri) {
      sharePdf(uiState.savedFileUri)
    }
  }
  HedvigScaffold(
    navigateUp,
    topAppBarText = stringResource(R.string.TIER_FLOW_SUMMARY_TITLE),
  ) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    if (showConfirmationDialog) {
      HedvigAlertDialog(
        title = stringResource(R.string.TIER_FLOW_CONFIRMATION_DIALOG_TEXT),
        onDismissRequest = { showConfirmationDialog = false },
        onConfirmClick = onConfirmClick,
        confirmButtonLabel = stringResource(R.string.GENERAL_CONFIRM),
        dismissButtonLabel = stringResource(R.string.general_cancel_button),
        text = null,
      )
    }
    SummaryCard(
      uiState = uiState,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      downloadFromUrl = downloadFromUrl,
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    Column(
      Modifier
        .padding(horizontal = 16.dp),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(
            stringResource(R.string.TIER_FLOW_TOTAL),
            style = HedvigTheme.typography.bodySmall,
          )
        },
        spaceBetween = 8.dp,
        endSlot = {
          HedvigText(
            text = uiState.quote.premium.toString(),
            textAlign = TextAlign.End,
            style = HedvigTheme.typography.bodySmall,
          )
        },
      )
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(R.string.TIER_FLOW_SUMMARY_CONFIRM_BUTTON),
        modifier = Modifier.fillMaxWidth(),
        buttonStyle = Primary,
        buttonSize = Large,
        enabled = true,
        onClick = {
          showConfirmationDialog = true
        },
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun SummaryCard(
  uiState: SummaryState.Success,
  downloadFromUrl: (url: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showExpanded by remember { mutableStateOf(false) }
  Surface(
    modifier = modifier,
    shape = HedvigTheme.shapes.cornerXLarge,
  ) {
    Column(Modifier.padding(16.dp)) {
      PillAndBasicInfo(
        contractGroup = uiState.currentContractData.contractGroup,
        displayName = uiState.quote.productVariant.displayName,
        displaySubtitle = uiState.currentContractData.contractDisplaySubtitle,
      )
      Spacer(Modifier.height(16.dp))
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(
            stringResource(R.string.TIER_FLOW_TOTAL),
            style = HedvigTheme.typography.bodySmall,
          )
        },
        spaceBetween = 8.dp,
        endSlot = {
          HedvigText(
            text = uiState.quote.premium.toString(),
            textAlign = TextAlign.End,
            style = HedvigTheme.typography.bodySmall,
          )
        },
      )
      HedvigText(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.End,
        text = stringResource(
          R.string.TIER_FLOW_PREVIOUS_PRICE,
          uiState.currentContractData.activeDisplayPremium.toString(),
        ),
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
      AnimatedVisibility(showExpanded) {
        ExtendedCardContent(
          quote = uiState.quote,
          downloadFromUrl = downloadFromUrl,
          isPDFLoading = uiState.isLoadingPdf,
        )
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        modifier = Modifier.fillMaxWidth(),
        text = if (showExpanded) {
          stringResource(R.string.TIER_FLOW_SUMMARY_HIDE_DETAILS_BUTTON)
        } else {
          stringResource(R.string.TIER_FLOW_SUMMARY_SHOW_DETAILS)
        },
        onClick = { showExpanded = !showExpanded },
        enabled = true,
        buttonStyle = Secondary,
        buttonSize = ButtonSize.Medium,
      )
    }
  }
}

@Composable
fun DisplayItemRowSecondaryColor(leftText: String, rightText: String) {
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      HedvigText(leftText, color = HedvigTheme.colorScheme.textSecondary)
    },
    endSlot = {
      Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        HedvigText(
          rightText,
          color = HedvigTheme.colorScheme.textSecondary,
          textAlign = TextAlign.End,
        )
      }
    },
    spaceBetween = 8.dp,
  )
}

@Composable
private fun ExtendedCardContent(
  isPDFLoading: Boolean,
  quote: TierDeductibleQuote,
  downloadFromUrl: (url: String) -> Unit,
) {
  Column {
    Spacer(Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(Modifier.height(16.dp))
    HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_OVERVIEW_SUBTITLE))
    quote.displayItems.forEachIndexed { _, item ->
      DisplayItemRowSecondaryColor(item.displayTitle, item.displayValue)
    }
    Spacer(Modifier.height(16.dp))
    HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_COVERAGE_SUBTITLE))
    quote.productVariant.insurableLimits.forEachIndexed { i, insurableLimit ->
      DisplayItemRowSecondaryColor(insurableLimit.label, insurableLimit.limit)
    }
    Spacer(Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_DOCUMENTS_SUBTITLE))
      Spacer(Modifier.width(16.dp))
      AnimatedVisibility(isPDFLoading) { HedvigThreeDotsProgressIndicator() }
    }
    quote.productVariant.documents.forEach { document ->
      DocumentRow(
        name = document.displayName,
        downloadFromUrl = {
          downloadFromUrl(document.url)
        },
      )
    }
  }
}

@Composable
private fun DocumentRow(name: String, downloadFromUrl: () -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.clickable { downloadFromUrl() }
  ) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          HedvigText(
            color = HedvigTheme.colorScheme.textSecondary,
            text = buildAnnotatedString {
              append(name)
              withStyle(
                SpanStyle(
                  baselineShift = BaselineShift(0.3f),
                  fontSize = HedvigTheme.typography.label.fontSize,
                  color = HedvigTheme.colorScheme.textSecondary,
                ),
              ) {
                append("PDF")
              }
            },
          )
        }
      },
      endSlot = {
        Row(horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
          ) {
          IconButton(
            modifier = Modifier.size(24.dp),
            onClick = {
              downloadFromUrl()
            },
          ) {
            Icon(HedvigIcons.ArrowNorthEast, null)
          }
        }
      },
      spaceBetween = 8.dp,
    )
  }
}
