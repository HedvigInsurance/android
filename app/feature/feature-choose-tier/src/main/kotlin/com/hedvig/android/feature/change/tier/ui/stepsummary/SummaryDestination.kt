package com.hedvig.android.feature.change.tier.ui.stepsummary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.stringWithShiftedLabel
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
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
import com.hedvig.android.feature.change.tier.ui.stepcustomize.ContractData
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
  SummaryScreen(
    uiState = uiState,
    onReload = {
      viewModel.emit(SummaryEvent.Reload)
    },
    onSuccess = {
      viewModel.emit(SummaryEvent.ClearNavigation)
      onSuccess()
    },
    onFailure = {
      viewModel.emit(SummaryEvent.ClearNavigation)
      onFailure()
    },
    sharePdf = { file ->
      viewModel.emit(SummaryEvent.HandledSharingPdfFile)
      sharePdf(file)
    },
    navigateUp = navigateUp,
    onSubmitQuoteClick = {
      viewModel.emit(SummaryEvent.SubmitQuote)
    },
    downloadFromUrl = { url ->
      viewModel.emit(SummaryEvent.DownLoadFromUrl(url))
    },
  )
}

@Composable
private fun SummaryScreen(
  uiState: SummaryState,
  onReload: () -> Unit,
  onSuccess: () -> Unit,
  navigateUp: () -> Unit,
  onFailure: () -> Unit,
  sharePdf: (File) -> Unit,
  onSubmitQuoteClick: () -> Unit,
  downloadFromUrl: (url: String) -> Unit,
) {
  when (uiState) {
    Failure -> HedvigScaffold(navigateUp) {
      Spacer(Modifier.weight(1f))
      HedvigErrorSection(
        modifier = Modifier.fillMaxSize(),
        onButtonClick = onReload,
      )
      Spacer(Modifier.weight(1f))
    }

    Loading -> HedvigFullScreenCenterAlignedProgress()

    is MakingChanges -> {
      LaunchedEffect(uiState.navigateToSuccess) {
        val success = uiState.navigateToSuccess
        if (success) {
          onSuccess()
        }
      }
      MakingChangesScreen()
    }

    is Success -> {
      LaunchedEffect(uiState.navigateToFail) {
        val fail = uiState.navigateToFail
        if (fail) {
          onFailure()
        }
      }

      SummarySuccessScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        onConfirmClick = onSubmitQuoteClick,
        downloadFromUrl = downloadFromUrl,
        sharePdf = { file ->
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
  uiState: Success,
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
        subtitle = null,
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
            text = stringResource(R.string.TERMINATION_FLOW_PAYMENT_PER_MONTH, uiState.quote.premium.amount.toInt()),
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
private fun SummaryCard(uiState: Success, downloadFromUrl: (url: String) -> Unit, modifier: Modifier = Modifier) {
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
            text = stringResource(R.string.TERMINATION_FLOW_PAYMENT_PER_MONTH, uiState.quote.premium.amount.toInt()),
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
    modifier = Modifier.clickable { downloadFromUrl() },
  ) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          HedvigText(
            color = HedvigTheme.colorScheme.textSecondary,
            text = stringWithShiftedLabel(
              text = name,
              labelText = "PDF",
              textColor = HedvigTheme.colorScheme.textSecondary,
              textFontSize = HedvigTheme.typography.bodySmall.fontSize,
              labelFontSize = HedvigTheme.typography.label.fontSize,
            ),
          )
        }
      },
      endSlot = {
        Row(
          horizontalArrangement = Arrangement.End,
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

@HedvigMultiScreenPreview
@Composable
private fun PreviewChooseInsuranceScreen(
  @PreviewParameter(
    ChooseInsuranceUiStateProvider::class,
  ) uiState: SummaryState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SummaryScreen(
        uiState,
        {},
        {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class ChooseInsuranceUiStateProvider :
  CollectionPreviewParameterProvider<SummaryState>(
    listOf(
      Success(
        currentContractData = ContractData(
          contractGroup = ContractGroup.HOMEOWNER,
          contractDisplayName = "Home Homeowner",
          contractDisplaySubtitle = "Addressvägen 777",
          activeDisplayPremium = "449 kr",
        ),
        quote = TierDeductibleQuote(
          id = "id4",
          deductible = Deductible(
            UiMoney(3500.0, SEK),
            deductiblePercentage = 25,
            description = "En fast del och en rörlig del om 25% av skadekostnaden",
          ),
          displayItems = listOf(),
          premium = UiMoney(655.0, SEK),
          tier = Tier(
            "STANDARD",
            tierLevel = 1,
            tierDescription = "Vårt mellanpaket med hög ersättning.",
            tierDisplayName = "Standard",
          ),
          productVariant = ProductVariant(
            displayName = "Test",
            contractGroup = ContractGroup.RENTAL,
            contractType = ContractType.SE_APARTMENT_RENT,
            partner = "test",
            perils = listOf(),
            insurableLimits = listOf(),
            documents = listOf(),
            displayTierName = "Standard",
            tierDescription = "Our most standard coverage",
          ),
        ),
      ),
      Failure,
      Loading,
      MakingChanges(false),
    ),
  )
