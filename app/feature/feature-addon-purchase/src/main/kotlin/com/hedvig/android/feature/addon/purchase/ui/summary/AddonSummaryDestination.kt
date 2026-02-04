package com.hedvig.android.feature.addon.purchase.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.ItemCostDiscount
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.DialogDefaults
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.feature.addon.purchase.data.CurrentlyActiveAddon
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuoteInsuranceDocument
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Content
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Loading
import com.hedvig.android.logger.logcat
import com.hedvig.android.tiersandaddons.CostBreakdownEntry
import com.hedvig.android.tiersandaddons.DisplayDocument
import com.hedvig.android.tiersandaddons.QuoteCard
import com.hedvig.android.tiersandaddons.QuoteDisplayItem
import hedvig.resources.ADDON_FLOW_CONFIRMATION_BUTTON
import hedvig.resources.ADDON_FLOW_CONFIRMATION_DESCRIPTION
import hedvig.resources.ADDON_FLOW_CONFIRMATION_TITLE
import hedvig.resources.ADDON_FLOW_PRICE_LABEL
import hedvig.resources.ADDON_FLOW_SUMMARY_ACTIVE_FROM
import hedvig.resources.ADDON_FLOW_SUMMARY_CONFIRM_BUTTON
import hedvig.resources.ADDON_FLOW_SUMMARY_INFO_TEXT
import hedvig.resources.ADDON_FLOW_SUMMARY_PRICE_SUBTITLE
import hedvig.resources.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_SUMMARY_TITLE
import hedvig.resources.TIER_FLOW_TOTAL
import hedvig.resources.general_close_button
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AddonSummaryDestination(
  viewModel: AddonSummaryViewModel,
  navigateUp: () -> Unit,
  onFailure: () -> Unit,
  onSuccess: (activationDate: LocalDate) -> Unit,
) {
  val uiState: AddonSummaryState by viewModel.uiState.collectAsStateWithLifecycle()
  AddonSummaryScreen(
    uiState = uiState,
    onSuccess = { date ->
      viewModel.emit(AddonSummaryEvent.ReturnToInitialState)
      onSuccess(date)
    },
    onFailure = {
      viewModel.emit(AddonSummaryEvent.ReturnToInitialState)
      onFailure()
    },
    navigateUp = navigateUp,
    onSubmitQuoteClick = {
      viewModel.emit(AddonSummaryEvent.Submit)
    },
  )
}

@Composable
private fun AddonSummaryScreen(
  uiState: AddonSummaryState,
  onSuccess: (LocalDate) -> Unit,
  navigateUp: () -> Unit,
  onFailure: () -> Unit,
  onSubmitQuoteClick: () -> Unit,
) {
  when (uiState) {
    is Loading -> {
      LaunchedEffect(uiState.activationDateToNavigateToSuccess) {
        val date = uiState.activationDateToNavigateToSuccess
        if (date != null) {
          onSuccess(date)
        }
      }

      HedvigFullScreenCenterAlignedProgress()
    }

    is Content -> {
      LaunchedEffect(uiState.navigateToFailure) {
        val fail = uiState.navigateToFailure
        if (fail!=null) {
          onFailure()
        }
      }

      SummarySuccessScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        onConfirmClick = onSubmitQuoteClick,
      )
    }
  }
}

@Composable
private fun SummarySuccessScreen(uiState: Content, onConfirmClick: () -> Unit, navigateUp: () -> Unit) {
  HedvigScaffold(
    navigateUp,
    topAppBarText = stringResource(Res.string.TIER_FLOW_SUMMARY_TITLE),
  ) {
    val locale = getLocale()
    val formattedDate = remember(uiState.activationDate, locale) {
      HedvigDateTimeFormatterDefaults.dateMonthAndYear(
        locale,
      ).format(uiState.activationDate)
    }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    if (showConfirmationDialog) {
      HedvigAlertDialog(
        title = stringResource(Res.string.ADDON_FLOW_CONFIRMATION_TITLE),
        onDismissRequest = { showConfirmationDialog = false },
        onConfirmClick = onConfirmClick,
        buttonSize = DialogDefaults.ButtonSize.BIG,
        confirmButtonLabel = stringResource(Res.string.ADDON_FLOW_CONFIRMATION_BUTTON),
        dismissButtonLabel = stringResource(Res.string.general_close_button),
        text = stringResource(
          Res.string.ADDON_FLOW_CONFIRMATION_DESCRIPTION,
          formattedDate,
        ),
      )
    }
    SummaryCard(
      uiState = uiState,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    Column(
      Modifier
        .padding(horizontal = 16.dp),
    ) {
      uiState.notificationMessage?.let {
        HedvigNotificationCard(
          message = uiState.notificationMessage,
          priority = NotificationDefaults.NotificationPriority.Info,
        )
        Spacer(Modifier.height(24.dp))
      }
      HorizontalItemsWithMaximumSpaceTaken(
        modifier = Modifier.semantics(true) {},
        startSlot = {
          HedvigText(
            stringResource(Res.string.TIER_FLOW_TOTAL),
            style = HedvigTheme.typography.bodySmall,
          )
        },
        spaceBetween = 8.dp,
        endSlot = {
          val totalExtra = uiState.costBreakdownWithExtras?.totalExtra
          if (totalExtra!=null) {
            val text = if (totalExtra.amount > 0) {
              // with +
              stringResource(
                Res.string.ADDON_FLOW_PRICE_LABEL,
                totalExtra,
              )
            } else {
              // without + (supposedly with minus)
              stringResource(
                Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                totalExtra,
              )
            }
            val voiceDescription = totalExtra.getPerMonthDescription()
            HedvigText(
              text = text,
              textAlign = TextAlign.End,
              style = HedvigTheme.typography.bodySmall,
              modifier = Modifier.semantics(true) {
                contentDescription = voiceDescription
              },
            )
          }

        },
      )
      Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
      ) {
        HedvigText(
          text = stringResource(Res.string.ADDON_FLOW_SUMMARY_PRICE_SUBTITLE),
          textAlign = TextAlign.End,
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(Res.string.ADDON_FLOW_SUMMARY_CONFIRM_BUTTON),
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
private fun SummaryCard(uiState: Content, modifier: Modifier = Modifier) {
  val locale = getLocale()
  val formattedDate = remember(uiState.activationDate, locale) {
    HedvigDateTimeFormatterDefaults.dateMonthAndYear(
      locale,
    ).format(uiState.activationDate)
  }
  val premium: UiMoney? = uiState.costBreakdownWithExtras?.totalCost?.monthlyNet
  val previousPremium: UiMoney? = if (premium==uiState.costBreakdownWithExtras?.totalCost?.monthlyGross) {
    null
  } else {
    uiState.costBreakdownWithExtras?.totalCost?.monthlyGross
  }
  val costBreakdown: List<CostBreakdownEntry> = uiState.costBreakdownWithExtras?.displayItems?.map{
    CostBreakdownEntry(
      displayName = it.first,
      displayValue =  stringResource(
        Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
        it.second, //todo: check!!
      ),
      hasStrikethrough = false
    )
  } ?: emptyList() //todo: should be not-null, wait for BE
//  val costBreakdown: List<CostBreakdownEntry> =
//    if (uiState.currentlyActiveAddon != null) {
//      val currentAddonDisplayItemValue = stringResource(
//        Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
//        uiState.currentlyActiveAddon.cost.monthlyNet, //todo: check!!
//      )
//      val newAddonDisplayValueNet = stringResource(
//        Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
//        uiState.quote.itemCost.monthlyNet,
//      )
//      buildList {
//        add(
//          CostBreakdownEntry(
//            uiState.currentlyActiveAddon.displayTitle,
//            currentAddonDisplayItemValue,
//            true,
//          ),
//        )
//        add(
//          CostBreakdownEntry(
//            uiState.quote.displayDescription,
//            newAddonDisplayValueNet,
//            false,
//          ),
//        )
//      }
//    } else {
//      val newAddonDisplayValueGross = stringResource(
//        Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
//        uiState.quote.itemCost.monthlyGross,
//      )
//      buildList {
//        add(
//          CostBreakdownEntry(
//            uiState.quote.displayDescription,
//            newAddonDisplayValueGross,
//            false,
//          ),
//        )
//        uiState.quote.itemCost.discounts.forEach { discount ->
//          add(
//            CostBreakdownEntry(
//              discount.displayName,
//              discount.displayValue,
//              false,
//            ),
//          )
//        }
//      }
//    }
  QuoteCard(
    subtitle = stringResource(
      Res.string.ADDON_FLOW_SUMMARY_ACTIVE_FROM,
      formattedDate,
    ),
    contractGroup = uiState.contractGroup,
    premium = premium ?: UiMoney(0.0, UiCurrencyCode.SEK), //todo: should be notnull, wait for BE
    costBreakdown = costBreakdown,
    previousPremium = previousPremium,
    displayItems = uiState.displayItems.map {
      QuoteDisplayItem(
        title = it.first,
        subtitle = null,
        value = it.second,
      )
    },
    modifier = modifier,
    displayName = uiState.insuranceDisplayName,
    insurableLimits = emptyList(),
    documents = uiState.documents.map {
      DisplayDocument(
        displayName = it.displayName,
        url = it.url
      )
    },
  )
}

@Composable
private fun AddonCostBreakdownComposable(
  currentlyActiveAddon: CurrentlyActiveAddon?,
  quote: AddonQuote,
  modifier: Modifier = Modifier,
) {
  ProvideTextStyle(HedvigTheme.typography.label.copy(color = HedvigTheme.colorScheme.textSecondary)) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Spacer(Modifier.height(8.dp))
      if (currentlyActiveAddon != null) {
        HorizontalItemsWithMaximumSpaceTaken(
          {
            HedvigText(
              currentlyActiveAddon.displayTitle,
              style = LocalTextStyle.current.copy(
                textDecoration = TextDecoration.LineThrough,
              ),
            )
          },
          {
            HedvigText(
              text = stringResource(
                Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                currentlyActiveAddon.cost.monthlyNet, //todo: CHECK!!!
              ),
              textAlign = TextAlign.End,
              style = LocalTextStyle.current.copy(
                textDecoration = TextDecoration.LineThrough,
              ),
            )
          },
          spaceBetween = 8.dp,
        )
        HorizontalItemsWithMaximumSpaceTaken(
          { HedvigText(quote.displayDescription) },
          {
            HedvigText(
              text = stringResource(
                Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                quote.itemCost.monthlyNet,
              ),
              textAlign = TextAlign.End,
            )
          },
          spaceBetween = 8.dp,
        )
      } else {
        HorizontalItemsWithMaximumSpaceTaken(
          { HedvigText(quote.displayDescription) },
          {
            HedvigText(
              text = stringResource(
                Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                quote.itemCost.monthlyGross,
              ),
              textAlign = TextAlign.End,
            )
          },
          spaceBetween = 8.dp,
        )
        quote.itemCost.discounts.forEach { discount ->
          HorizontalItemsWithMaximumSpaceTaken(
            { HedvigText(discount.displayName) },
            {
              HedvigText(
                text = discount.displayValue,
                textAlign = TextAlign.End,
              )
            },
            spaceBetween = 8.dp,
          )
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseInsuranceToTerminateScreen(
  @PreviewParameter(
    ChooseInsuranceForAddonUiStateProvider::class,
  ) uiState: AddonSummaryState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AddonSummaryScreen(
        uiState,
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class ChooseInsuranceForAddonUiStateProvider :
  CollectionPreviewParameterProvider<AddonSummaryState>(
    listOf(
      Loading(      activationDateToNavigateToSuccess = null),
      Content(
        currentlyActiveAddons = listOf(
          CurrentlyActiveAddon(
            displayTitle = "Travel Plus 45 days",
            displayDescription = "description",
            cost = ItemCost(
              UiMoney(49.0, UiCurrencyCode.SEK),
              UiMoney(49.0, UiCurrencyCode.SEK),
              emptyList()
            ),
          )
        ),
        insuranceDisplayName = "TravelPlus",
        activationDate = LocalDate(2025, 1, 1),
        quotes = listOf(
          AddonQuote(
            displayTitle = "60 days",
            addonId = "addonId1",
            displayDetails = listOf(
              "Amount of insured people" to "You +1",
              "Coverage" to "60 days",
            ),
            addonVariant = AddonVariant(
              termsVersion = "terms",
              documents = listOf(),
              perils = listOf(),
              displayName = "60 days",
              product = "",
            ),
            displayDescription = "Travel Plus 60 days",
            documents = listOf(
              TravelAddonQuoteInsuranceDocument(
                displayName = "Document display name",
                url = ""
              )
            ),
            itemCost = ItemCost(
              UiMoney(79.0, UiCurrencyCode.SEK),
              UiMoney(89.0, UiCurrencyCode.SEK),
              discounts = listOf(
                ItemCostDiscount(
                  campaignCode = "Bundle",
                  displayName = "15% bundle discount",
                  displayValue = "-19kr/mo",
                  explanation = "some explanation",
                ),
              ),
            ),
            addonSubtype = "DAYS_60"
          )
        ),

        navigateToFailure = null,
        insuranceExposure = "Exposure",
        notificationMessage = "Notification message",
        documents = emptyList(),
        costBreakdownWithExtras = null,
        displayItems = emptyList(),
        contractGroup = null
      ),
      Content(
        currentlyActiveAddons = emptyList(),
        insuranceDisplayName = "TravelPlus",
        activationDate = LocalDate(2025, 1, 1),
        quotes = listOf(
          AddonQuote(
            displayTitle = "60 days",
            addonId = "addonId1",
            displayDetails = listOf(
              "Amount of insured people" to "You +1",
              "Coverage" to "60 days",
            ),
            addonVariant = AddonVariant(
              termsVersion = "terms",
              documents = listOf(),
              perils = listOf(),
              displayName = "60 days",
              product = "",
            ),
            displayDescription = "Travel Plus 60 days",
            documents = listOf(
              TravelAddonQuoteInsuranceDocument(
                displayName = "Document display name",
                url = ""
              )
            ),
            itemCost = ItemCost(
              UiMoney(40.0, UiCurrencyCode.SEK),
              UiMoney(89.0, UiCurrencyCode.SEK),
              discounts = listOf(
                ItemCostDiscount(
                  campaignCode = "Bundle",
                  displayName = "15% bundle discount",
                  displayValue = "-10 kr/mo",
                  explanation = "some explanation",
                ),
                ItemCostDiscount(
                  campaignCode = "TRALALA",
                  displayName = "50% discount for 3 months",
                  displayValue = "-39 kr/mo",
                  explanation = "some explanation",
                ),
              ),
            ),
            addonSubtype = "DAYS_60"
          )
        ),
        navigateToFailure = null,
        insuranceExposure = "Exposure",
        notificationMessage = "Notification message",
        documents = emptyList(),
        costBreakdownWithExtras = CostBreakdownWithExtras(
          totalCost = ItemCost(
            monthlyNet = UiMoney(250.0, UiCurrencyCode.SEK),
            monthlyGross = UiMoney(250.0, UiCurrencyCode.SEK),
            discounts = emptyList()
          ),
          totalExtra = UiMoney(50.0, UiCurrencyCode.SEK),
          displayItems = listOf(
            "base insurance" to UiMoney(200.0, UiCurrencyCode.SEK),
            "addon" to UiMoney(50.0, UiCurrencyCode.SEK),
          )
        ),
        displayItems = emptyList(),
        contractGroup = null
      ),
    ),
  )
