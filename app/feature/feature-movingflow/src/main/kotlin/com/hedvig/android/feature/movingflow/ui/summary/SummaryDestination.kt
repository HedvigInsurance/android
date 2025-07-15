package com.hedvig.android.feature.movingflow.ui.summary

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.InsuranceVariantDocument.InsuranceDocumentType.CERTIFICATE
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.data.productvariant.ProductVariantPeril
import com.hedvig.android.design.system.hedvig.AccordionData
import com.hedvig.android.design.system.hedvig.AccordionList
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.feature.movingflow.data.AddonId
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.AddonQuote.HomeAddonQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.AddonQuote.MtaAddonQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.DisplayItem
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote.Deductible
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveMtaQuote
import com.hedvig.android.feature.movingflow.ui.MovingFlowTopAppBar
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content.SubmitError.Generic
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content.SubmitError.WithMessage
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Loading
import com.hedvig.android.tiersandaddons.QuoteCard
import com.hedvig.android.tiersandaddons.QuoteCardDefaults
import com.hedvig.android.tiersandaddons.QuoteCardState
import com.hedvig.android.tiersandaddons.QuoteDisplayItem
import com.hedvig.android.tiersandaddons.rememberQuoteCardState
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun SummaryDestination(
  viewModel: SummaryViewModel,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  exitFlow: () -> Unit,
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
    exitFlow = exitFlow,
    toggleHomeAddonExclusion = { viewModel.emit(SummaryEvent.ToggleHomeAddonExclusion(it)) },
    onConfirmChanges = { viewModel.emit(SummaryEvent.ConfirmChanges) },
    onDismissSubmissionError = { viewModel.emit(SummaryEvent.DismissSubmissionError) },
  )
}

@Composable
private fun SummaryScreen(
  uiState: SummaryUiState,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  exitFlow: () -> Unit,
  toggleHomeAddonExclusion: (AddonId) -> Unit,
  onConfirmChanges: () -> Unit,
  onDismissSubmissionError: () -> Unit,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column {
      MovingFlowTopAppBar(
        navigateUp = navigateUp,
        exitFlow = exitFlow,
        topAppBarText = stringResource(R.string.CHANGE_ADDRESS_SUMMARY_TITLE),
      )
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        propagateMinConstraints = true,
      ) {
        when (uiState) {
          Loading -> HedvigFullScreenCenterAlignedProgress()

          SummaryUiState.Error -> HedvigErrorSection(
            onButtonClick = navigateBack,
            subTitle = null,
            buttonText = stringResource(R.string.general_back_button),
          )

          is Content -> {
            SummaryScreen(
              content = uiState,
              toggleHomeAddonExclusion = toggleHomeAddonExclusion,
              onConfirmChanges = onConfirmChanges,
              onDismissSubmissionError = onDismissSubmissionError,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SummaryScreen(
  content: SummaryUiState.Content,
  toggleHomeAddonExclusion: (AddonId) -> Unit,
  onConfirmChanges: () -> Unit,
  onDismissSubmissionError: () -> Unit,
) {
  val startDateFormatted = formatStartDate(content.summaryInfo.moveHomeQuote.startDate)
  var showConfirmChangesDialog by rememberSaveable { mutableStateOf(false) }
  if (showConfirmChangesDialog) {
    HedvigAlertDialog(
      title = stringResource(R.string.CHANGE_ADDRESS_ACCEPT_OFFER),
      onDismissRequest = { showConfirmChangesDialog = false },
      onConfirmClick = onConfirmChanges,
      confirmButtonLabel = stringResource(R.string.GENERAL_CONFIRM),
      dismissButtonLabel = stringResource(R.string.general_cancel_button),
      text = stringResource(R.string.CONFIRM_CHANGES_SUBTITLE, startDateFormatted),
    )
  }
  if (content.submitError != null) {
    ErrorDialog(
      title = stringResource(R.string.something_went_wrong),
      message = when (content.submitError) {
        Generic -> stringResource(R.string.GENERAL_ERROR_BODY)
        is WithMessage -> content.submitError.message
      },
      buttonText = stringResource(R.string.general_close_button),
      onButtonClick = onDismissSubmissionError,
      onDismiss = onDismissSubmissionError,
    )
  }
  val exclusionBottomSheetState = rememberHedvigBottomSheetState<ExclusionDialogInfo>()
  HedvigBottomSheet(exclusionBottomSheetState) { data ->
    FlowHeading(
      stringResource(R.string.ADDON_REMOVE_TITLE, data.addonName),
      stringResource(R.string.ADDON_REMOVE_DESCRIPTION),
      baseStyle = HedvigTheme.typography.bodySmall,
    )
    Spacer(Modifier.height(32.dp))
    HedvigButton(
      text = stringResource(R.string.ADDON_REMOVE_CONFIRM_BUTTON, data.addonName),
      onClick = {
        exclusionBottomSheetState.dismiss()
        toggleHomeAddonExclusion(data.addonId)
      },
      enabled = true,
      buttonSize = Large,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      text = stringResource(R.string.ADDON_REMOVE_CANCEL_BUTTON),
      onClick = { exclusionBottomSheetState.dismiss() },
      enabled = true,
      buttonStyle = Ghost,
      buttonSize = Large,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
  Box(propagateMinConstraints = true) {
    var bottomAttachedContentHeightPx by remember { mutableIntStateOf(0) }
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        .verticalScroll(rememberScrollState()),
    ) {
      Spacer(Modifier.height(16.dp))
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QuoteCard(content.summaryInfo.moveHomeQuote)
        for (addonQuote in content.summaryInfo.moveHomeQuote.relatedAddonQuotes) {
          AddonQuoteCard(
            quote = addonQuote,
            canExcludeAddons = content.canExcludeAddons,
            toggleHomeAddonExclusion = {
              if (!addonQuote.isExcludedByUser) {
                exclusionBottomSheetState.show(ExclusionDialogInfo(addonQuote.addonId, addonQuote.exposureName))
              } else {
                toggleHomeAddonExclusion(addonQuote.addonId)
              }
            },
          )
        }
        for (mtaQuote in content.summaryInfo.moveMtaQuotes) {
          QuoteCard(mtaQuote)
          for (addonQuote in mtaQuote.relatedAddonQuotes) {
            AddonQuoteCard(quote = addonQuote)
          }
        }
      }
      if (content.summaryInfo.moveMtaQuotes.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        HedvigNotificationCard(stringResource(R.string.CHANGE_ADDRESS_OTHER_INSURANCES_INFO_TEXT), Info)
      }
      Spacer(Modifier.height(16.dp))
      with(LocalDensity.current) {
        Spacer(Modifier.height(bottomAttachedContentHeightPx.toDp()))
      }
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
      modifier = Modifier
        .wrapContentHeight(Alignment.Bottom)
        .onPlaced {
          bottomAttachedContentHeightPx = it.size.height
        },
    ) {
      Column(
        Modifier
          .padding(horizontal = 16.dp)
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        Spacer(Modifier.height(16.dp))
        HorizontalItemsWithMaximumSpaceTaken(
          startSlot = {
            HedvigText(stringResource(R.string.TIER_FLOW_TOTAL))
          },
          endSlot = {
            AnimatedContent(
              targetState = content.summaryInfo.totalPremium,
              transitionSpec = {
                slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
              },
            ) { premium ->
              val voiceoverDescription = premium.getPerMonthDescription()
              HedvigText(
                text = stringResource(R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, premium.toString()),
                textAlign = TextAlign.End,
                modifier = Modifier
                  .wrapContentWidth(Alignment.End)
                  .semantics {
                    contentDescription = voiceoverDescription
                  },
              )
            }
          },
          modifier = Modifier.fillMaxWidth(),
          spaceBetween = 8.dp,
        )
        Spacer(Modifier.height(2.dp))
        HedvigText(
          text = stringResource(R.string.CHANGE_ADDRESS_ACTIVATION_DATE, startDateFormatted),
          style = HedvigTheme.typography.label.copy(color = HedvigTheme.colorScheme.textSecondary),
          textAlign = TextAlign.End,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = stringResource(R.string.CHANGE_ADDRESS_ACCEPT_OFFER),
          enabled = !content.shouldDisableInput,
          onClick = { showConfirmChangesDialog = true },
          isLoading = content.isSubmitting,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
      }
    }
  }
}

@Composable
private fun QuoteCard(
  quote: MovingFlowQuotes.Quote,
  modifier: Modifier = Modifier,
  underTitleContent: @Composable () -> Unit = {},
) {
  QuoteCard(
    productVariant = quote.productVariant,
    subtitle = quote.exposureName, // todo look into if this is the correct field to use
    premium = quote.premium,
    previousPremium = null, // todo bundle discounts
    displayItems = quote.displayItems.map {
      QuoteDisplayItem(
        title = it.title,
        subtitle = it.subtitle,
        value = it.value,
      )
    },
    modifier = modifier,
    underTitleContent = underTitleContent,
  )
}

@Composable
private fun AddonQuoteCard(
  quote: MovingFlowQuotes.AddonQuote.HomeAddonQuote,
  canExcludeAddons: Boolean,
  toggleHomeAddonExclusion: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AddonQuoteCard(
    quote = quote,
    modifier = modifier,
    underDetailsContent = { state ->
      LaunchedEffect(quote.isExcludedByUser) {
        if (quote.isExcludedByUser) {
          state.showDetails = false
        }
      }
      Column {
        Spacer(Modifier.height(16.dp))
        AnimatedVisibility(
          visible = canExcludeAddons && state.showDetails && !quote.isExcludedByUser,
          enter = expandVertically(expandFrom = Alignment.Top),
          exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
          HedvigButton(
            text = stringResource(R.string.GENERAL_REMOVE),
            onClick = toggleHomeAddonExclusion,
            enabled = true,
            buttonStyle = Ghost,
            buttonSize = Medium,
            border = HedvigTheme.colorScheme.borderPrimary,
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 8.dp),
          )
        }
        if (quote.isExcludedByUser) {
          HedvigButton(
            text = stringResource(R.string.ADDON_ADD_COVERAGE),
            onClick = toggleHomeAddonExclusion,
            enabled = true,
            buttonStyle = Secondary,
            buttonSize = Medium,
            modifier = Modifier.fillMaxWidth(),
          )
        } else {
          HedvigButton(
            text = if (state.showDetails) {
              stringResource(R.string.TIER_FLOW_SUMMARY_HIDE_DETAILS_BUTTON)
            } else {
              stringResource(R.string.TIER_FLOW_SUMMARY_SHOW_DETAILS)
            },
            onClick = state::toggleState,
            enabled = true,
            buttonStyle = Secondary,
            buttonSize = Medium,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    },
  )
}

@Composable
private fun AddonQuoteCard(quote: MovingFlowQuotes.AddonQuote.MtaAddonQuote, modifier: Modifier = Modifier) {
  AddonQuoteCard(
    quote = quote,
    modifier = modifier,
    underDetailsContent = null,
  )
}

@Composable
private fun AddonQuoteCard(
  quote: MovingFlowQuotes.AddonQuote,
  modifier: Modifier = Modifier,
  underDetailsContent: (@Composable (QuoteCardState) -> Unit)?,
) {
  val subtitle = if (quote is HomeAddonQuote && quote.isExcludedByUser) {
    null
  } else {
    quote.coverageDisplayName
  }
  val quoteCardState = rememberQuoteCardState()
  QuoteCard(
    quoteCardState = object : QuoteCardState {
      override var showDetails: Boolean
        get() = if (quote is HomeAddonQuote && quote.isExcludedByUser) false else quoteCardState.showDetails
        set(value) {
          if (quote is HomeAddonQuote && quote.isExcludedByUser) return
          quoteCardState.showDetails = value
        }
      override val isEnabled: Boolean
        get() = if (quote is HomeAddonQuote && quote.isExcludedByUser) false else quoteCardState.isEnabled
    },
    displayName = quote.addonVariant.displayName,
    contractGroup = null,
    insurableLimits = emptyList(),
    documents = quote.addonVariant.documents,
    subtitle = subtitle,
    premium = quote.premium,
    previousPremium = null, // todo bundle discounts
    isExcluded = when (quote) {
      is HomeAddonQuote -> quote.isExcludedByUser
      is MtaAddonQuote -> false
    },
    displayItems = quote.displayItems.map {
      QuoteDisplayItem(
        title = it.title,
        subtitle = it.subtitle,
        value = it.value,
      )
    },
    modifier = modifier,
    underDetailsContent = underDetailsContent ?: { QuoteCardDefaults.UnderDetailsContent(it) },
  )
}

@Composable
private fun formatStartDate(startDate: LocalDate): String {
  val locale = getLocale()
  return remember(startDate) {
    HedvigDateTimeFormatterDefaults.dateMonthAndYear(locale).format(startDate.toJavaLocalDate())
  }
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

private data class ExclusionDialogInfo(
  val addonId: AddonId,
  val addonName: String,
)

@HedvigMultiScreenPreview
@Preview(device = "spec:width=1080px,height=3800px,dpi=440")
@Composable
private fun PreviewSummaryScreen(
  @PreviewParameter(SummaryUiStateProvider::class) summaryUiState: SummaryUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SummaryScreen(
        uiState = summaryUiState,
        navigateUp = {},
        navigateBack = {},
        exitFlow = {},
        toggleHomeAddonExclusion = {},
        onConfirmChanges = {},
        onDismissSubmissionError = {},
      )
    }
  }
}

private class SummaryUiStateProvider : PreviewParameterProvider<SummaryUiState> {
  private val productVariant = ProductVariant(
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
        null,
      ),
    ),
    insurableLimits = listOf(
      InsurableLimit(
        label = "insurable limit label",
        limit = "insurable limit limit",
        description = "insurable limit description",
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
    termsVersion = "termsVersion",
  )
  private val addonVariant = AddonVariant(
    termsVersion = "terrrms",
    displayName = "Addon 1",
    product = "product",
    documents = listOf(
      InsuranceVariantDocument(
        displayName = "displayName",
        url = "url",
        type = CERTIFICATE,
      ),
    ),
    perils = listOf(),
  )
  val startDate = LocalDate.parse("2025-01-01")

  override val values: Sequence<SummaryUiState> = sequenceOf(
    SummaryUiState.Loading,
    SummaryUiState.Error,
    SummaryUiState.Content(
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
          relatedAddonQuotes = List(1) {
            HomeAddonQuote(
              addonId = AddonId(it.toString()),
              premium = UiMoney(129.0, SEK),
              startDate = startDate,
              displayItems = listOf(
                DisplayItem(
                  title = "display title",
                  subtitle = "display subtitle",
                  value = "display value",
                ),
              ),
              exposureName = "exposureName",
              addonVariant = addonVariant,
              isExcludedByUser = true,
              coverageDisplayName = "45 Days",
            )
          },
        ),
        moveMtaQuotes = listOf(
          MoveMtaQuote(
            premium = UiMoney(49.0, SEK),
            exposureName = "exposureName",
            productVariant = productVariant,
            startDate = startDate,
            displayItems = emptyList(),
            relatedAddonQuotes = emptyList(),
          ),
          MoveMtaQuote(
            premium = UiMoney(23.0, SEK),
            exposureName = "exposureName",
            productVariant = productVariant,
            startDate = startDate,
            displayItems = emptyList(),
            relatedAddonQuotes = listOf(
              MtaAddonQuote(
                addonId = AddonId("1"),
                premium = UiMoney(30.0, SEK),
                startDate = startDate,
                displayItems = listOf(
                  DisplayItem(
                    title = "display title",
                    subtitle = "display subtitle",
                    value = "display value",
                  ),
                ),
                exposureName = "exposureName",
                addonVariant = addonVariant,
                coverageDisplayName = "45 Days",
              ),
            ),
          ),
        ),
      ),
      false,
      null,
      null,
      true,
    ),
  )
}
