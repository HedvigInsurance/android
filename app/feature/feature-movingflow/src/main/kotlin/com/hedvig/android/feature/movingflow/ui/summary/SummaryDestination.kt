package com.hedvig.android.feature.movingflow.ui.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.InsurableLimit.InsurableLimitType.BIKE
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.InsuranceVariantDocument.InsuranceDocumentType.CERTIFICATE
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.data.productvariant.ProductVariantPeril
import com.hedvig.android.design.system.hedvig.AccordionData
import com.hedvig.android.design.system.hedvig.AccordionList
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Small
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigDialogError
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.ripple
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.DisplayItem
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote.Deductible
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveMtaQuote
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content.SubmitError.Generic
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content.SubmitError.WithMessage
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Loading
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun SummaryDestination(
  viewModel: SummaryViewModel,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateToFinishedScreen: (LocalDate) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  if (uiState is Content && uiState.navigateToFinishedScreenWithDate != null) {
    LaunchedEffect(uiState.navigateToFinishedScreenWithDate) {
      onNavigateToFinishedScreen(uiState.navigateToFinishedScreenWithDate)
    }
  }
  SummaryScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    navigateBack = navigateBack,
    onNavigateToNewConversation = onNavigateToNewConversation,
    onConfirmChanges = { viewModel.emit(SummaryEvent.ConfirmChanges) },
    onDismissSubmissionError = { viewModel.emit(SummaryEvent.DismissSubmissionError) },
  )
}

@Composable
private fun SummaryScreen(
  uiState: SummaryUiState,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onConfirmChanges: () -> Unit,
  onDismissSubmissionError: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.CHANGE_ADDRESS_SUMMARY_TITLE),
    modifier = Modifier.fillMaxSize(),
  ) {
    when (uiState) {
      Loading -> HedvigFullScreenCenterAlignedProgress()
      SummaryUiState.Error -> HedvigErrorSection(
        onButtonClick = navigateBack,
        subTitle = null,
        buttonText = stringResource(R.string.general_back_button),
      )

      is Content -> SummaryScreen(
        content = uiState,
        onNavigateToNewConversation = onNavigateToNewConversation,
        onConfirmChanges = onConfirmChanges,
        onDismissSubmissionError = onDismissSubmissionError,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun SummaryScreen(
  content: SummaryUiState.Content,
  onNavigateToNewConversation: () -> Unit,
  onConfirmChanges: () -> Unit,
  onDismissSubmissionError: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showConfirmChangesDialog by rememberSaveable { mutableStateOf(false) }
  if (showConfirmChangesDialog) {
    HedvigAlertDialog(
      title = stringResource(R.string.TIER_FLOW_CONFIRMATION_DIALOG_TEXT),
      onDismissRequest = { showConfirmChangesDialog = false },
      onConfirmClick = onConfirmChanges,
      confirmButtonLabel = stringResource(R.string.GENERAL_CONFIRM),
      dismissButtonLabel = stringResource(R.string.general_cancel_button),
      subtitle = null,
    )
  }
  if (content.submitError != null) {
    HedvigDialogError(
      titleText = stringResource(R.string.something_went_wrong),
      descriptionText = when (content.submitError) {
        Generic -> stringResource(R.string.GENERAL_ERROR_BODY)
        is WithMessage -> content.submitError.message
      },
      buttonText = stringResource(R.string.general_close_button),
      onButtonClick = onDismissSubmissionError,
      onDismissRequest = onDismissSubmissionError,
    )
  }
  Column(
    modifier = modifier
      .padding(horizontal = 16.dp)
      .verticalScroll(rememberScrollState()),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      QuoteCard(content.summaryInfo.moveHomeQuote)
      for (mtaQuote in content.summaryInfo.moveMtaQuotes) {
        QuoteCard(mtaQuote)
      }
    }
    Spacer(Modifier.height(16.dp))
    HedvigNotificationCard(stringResource(R.string.CHANGE_ADDRESS_OTHER_INSURANCES_INFO_TEXT), Info)
    Spacer(Modifier.height(24.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigText(stringResource(R.string.TIER_FLOW_TOTAL))
      },
      endSlot = {
        HedvigText(
          text = stringResource(
            R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
            content.summaryInfo.totalPremium.toString(),
          ),
          textAlign = TextAlign.End,
          modifier = Modifier.wrapContentWidth(Alignment.End),
        )
      },
      modifier = Modifier.fillMaxWidth(),
      spaceBetween = 8.dp,
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.CHANGE_ADDRESS_ACCEPT_OFFER),
      enabled = !content.shouldDisableInput,
      onClick = { showConfirmChangesDialog = true },
      isLoading = content.isSubmitting,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(40.dp))
    QuestionsAndAnswers()
    Spacer(Modifier.height(40.dp))
    Column {
      HedvigText(
        text = stringResource(R.string.SUBMIT_CLAIM_NEED_HELP_TITLE),
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentWidth(Alignment.CenterHorizontally),
      )
      Spacer(Modifier.height(12.dp))
      HedvigButton(
        text = stringResource(R.string.open_chat),
        enabled = true,
        onClick = onNavigateToNewConversation,
        buttonSize = Small,
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentWidth(Alignment.CenterHorizontally),
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun QuoteCard(quote: MovingFlowQuotes.Quote, modifier: Modifier = Modifier) {
  var showDetails by rememberSaveable { mutableStateOf(false) }
  HedvigCard(
    modifier = modifier,
    onClick = { showDetails = !showDetails },
    interactionSource = null,
    indication = ripple(bounded = true, radius = 1000.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row {
        Image(
          painter = painterResource(quote.productVariant.contractGroup.toPillow()),
          contentDescription = null,
          modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          HedvigText(
            text = quote.productVariant.displayName,
          )
          val locale = getLocale()
          val startDate = remember(quote.startDate) {
            HedvigDateTimeFormatterDefaults.dateMonthAndYear(locale).format(quote.startDate.toJavaLocalDate())
          }
          HedvigText(
            text = stringResource(R.string.CHANGE_ADDRESS_ACTIVATION_DATE, startDate),
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(stringResource(R.string.TIER_FLOW_TOTAL))
        },
        endSlot = {
          HedvigText(
            text = stringResource(
              R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              quote.premium.toString(),
            ),
            textAlign = TextAlign.End,
            modifier = Modifier.wrapContentWidth(Alignment.End),
          )
        },
      )
      AnimatedVisibility(
        visible = showDetails,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
      ) {
        Column {
          Spacer(Modifier.height(16.dp))
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            HorizontalDivider()
            if (quote.displayItems.isNotEmpty()) {
              Column {
                HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_OVERVIEW_SUBTITLE))
                for (displayItem in quote.displayItems) {
                  InfoRow(displayItem.title, displayItem.value)
                }
              }
            }
            if (quote.productVariant.insurableLimits.isNotEmpty()) {
              Column {
                HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_COVERAGE_SUBTITLE))
                for (insurableLimit in quote.productVariant.insurableLimits) {
                  InfoRow(
                    insurableLimit.label,
                    insurableLimit.limit,
                  )
                }
              }
            }
            if (quote.productVariant.documents.isNotEmpty()) {
              Column {
                HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_DOCUMENTS_SUBTITLE))
                for (document in quote.productVariant.documents) {
                  val uriHandler = LocalUriHandler.current
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(HedvigTheme.shapes.cornerExtraSmall)
                      .clickable {
                        uriHandler.openUri(document.url)
                      },
                  ) {
                    HedvigText(
                      text = document.displayName,
                      color = HedvigTheme.colorScheme.textSecondary,
                      modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                      imageVector = HedvigIcons.ArrowNorthEast,
                      contentDescription = null,
                      tint = HedvigTheme.colorScheme.fillPrimary,
                      modifier = Modifier.wrapContentWidth(),
                    )
                  }
                }
              }
            }
          }
        }
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = if (showDetails) {
          stringResource(R.string.TIER_FLOW_SUMMARY_HIDE_DETAILS_BUTTON)
        } else {
          stringResource(R.string.TIER_FLOW_SUMMARY_SHOW_DETAILS)
        },
        onClick = { showDetails = !showDetails },
        enabled = true,
        buttonStyle = Secondary,
        buttonSize = Medium,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun InfoRow(leftText: String, rightText: String, modifier: Modifier = Modifier) {
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      HedvigText(leftText, color = HedvigTheme.colorScheme.textSecondary)
    },
    endSlot = {
      HedvigText(
        text = rightText,
        color = HedvigTheme.colorScheme.textSecondary,
        textAlign = TextAlign.End,
        modifier = Modifier.wrapContentWidth(Alignment.End),
      )
    },
    modifier = modifier,
    spaceBetween = 8.dp,
  )
}

@Composable
private fun QuestionsAndAnswers(modifier: Modifier = Modifier) {
  val faqs = remember {
    listOf(
      R.string.CHANGE_ADDRESS_FAQ_DATE_TITLE to R.string.CHANGE_ADDRESS_FAQ_DATE_LABEL,
      R.string.CHANGE_ADDRESS_FAQ_PRICE_TITLE to R.string.CHANGE_ADDRESS_FAQ_PRICE_LABEL,
      R.string.CHANGE_ADDRESS_FAQ_RENTBRF_TITLE to R.string.CHANGE_ADDRESS_FAQ_RENTBRF_LABEL,
      R.string.CHANGE_ADDRESS_FAQ_STORAGE_TITLE to R.string.CHANGE_ADDRESS_FAQ_STORAGE_LABEL,
      R.string.CHANGE_ADDRESS_FAQ_STUDENT_TITLE to R.string.CHANGE_ADDRESS_FAQ_STUDENT_LABEL,
    )
  }
  Column(modifier) {
    HedvigText(stringResource(R.string.CHANGE_ADDRESS_QA))
    Spacer(Modifier.height(24.dp))
    AccordionList(
      items = faqs.map { (title, description) ->
        AccordionData(
          title = stringResource(title),
          description = stringResource(description),
        )
      },
    )
  }
}

@HedvigPreview
@Preview(device = "spec:width=1080px,height=3800px,dpi=440")
@Composable
private fun PreviewSummaryScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      val productVariant = ProductVariant(
        displayName = "Variant",
        contractGroup = ContractGroup.RENTAL,
        contractType = ContractType.SE_APARTMENT_RENT,
        partner = null,
        perils = listOf(
          ProductVariantPeril(
            "id",
            "peril title",
            "peril description",
            emptyList(),
            emptyList(),
            null,
          ),
        ),
        insurableLimits = listOf(
          InsurableLimit(
            label = "insurable limit label",
            limit = "insurable limit limit",
            description = "insurable limit description",
            type = BIKE,
          ),
        ),
        documents = listOf(
          InsuranceVariantDocument(
            displayName = "displayName",
            url = "url",
            type = CERTIFICATE,
          ),
        ),
        displayTierName = "tierDescription",
        tierDescription = "displayNameTier",
      )
      val startDate = LocalDate.parse("2025-01-01")
      SummaryScreen(
        uiState = SummaryUiState.Content(
          summaryInfo = SummaryInfo(
            moveHomeQuote = MoveHomeQuote(
              id = "id",
              premium = UiMoney(99.0, SEK),
              startDate = startDate,
              displayItems = listOf(
                DisplayItem(
                  title = "display title",
                  subtitle = "display subtitle",
                  value = "display value",
                ),
              ),
              exposureName = "exposureName",
              productVariant = productVariant,
              tierName = "tierName",
              tierLevel = 1,
              tierDescription = "tierDescription",
              deductible = Deductible(UiMoney(1500.0, SEK), null, "displayText"),
              defaultChoice = false,
            ),
            moveMtaQuotes = listOf(
              MoveMtaQuote(
                premium = UiMoney(49.0, SEK),
                exposureName = "exposureName",
                productVariant = productVariant,
                startDate = startDate,
                displayItems = emptyList(),
              ),
            ),
          ),
          false,
          null,
          null,
        ),
        navigateUp = {},
        navigateBack = {},
        onNavigateToNewConversation = {},
        onConfirmChanges = {},
        onDismissSubmissionError = {},
      )
    }
  }
}
