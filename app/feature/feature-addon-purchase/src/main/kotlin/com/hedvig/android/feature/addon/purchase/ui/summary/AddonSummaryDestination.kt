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
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.data.CurrentlyActiveAddon
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuoteInsuranceDocument
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Content
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Loading
import com.hedvig.ui.tiersandaddons.CostBreakdownEntry
import com.hedvig.ui.tiersandaddons.DisplayDocument
import com.hedvig.ui.tiersandaddons.QuoteCard
import com.hedvig.ui.tiersandaddons.QuoteDisplayItem
import hedvig.resources.ADDON_FLOW_CONFIRMATION_BUTTON
import hedvig.resources.ADDON_FLOW_CONFIRMATION_TITLE
import hedvig.resources.ADDON_FLOW_PRICE_LABEL
import hedvig.resources.ADDON_FLOW_SUMMARY_CONFIRM_BUTTON
import hedvig.resources.ADDON_FLOW_SUMMARY_PRICE_SUBTITLE
import hedvig.resources.GENERAL_CHANGE_CONFIRMATION_DESCRIPTION
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
    reload = {
      viewModel.emit(AddonSummaryEvent.Reload)
    },
  )
}

@Composable
private fun AddonSummaryScreen(
  uiState: AddonSummaryState,
  onSuccess: (LocalDate) -> Unit,
  navigateUp: () -> Unit,
  onFailure: () -> Unit,
  reload: () -> Unit,
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
        if (fail != null) {
          onFailure()
        }
      }

      SummarySuccessScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        onConfirmClick = onSubmitQuoteClick,
      )
    }

    AddonSummaryState.Error -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
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
          Res.string.GENERAL_CHANGE_CONFIRMATION_DESCRIPTION,
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
          modifier = Modifier.fillMaxWidth(),
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
          if (totalExtra != null) {
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
  val premium: UiMoney? = uiState.costBreakdownWithExtras?.totalMonthlyNet
  val previousPremium: UiMoney? = if (premium == uiState.costBreakdownWithExtras?.totalMonthlyGross) {
    null
  } else {
    uiState.costBreakdownWithExtras?.totalMonthlyGross
  }
  val costBreakdown: List<CostBreakdownEntry> = uiState.costBreakdownWithExtras?.displayItems
    ?: emptyList()
  QuoteCard(
    subtitle = uiState.insuranceExposure,
    contractGroup = uiState.contractGroup,
    premium = premium ?: UiMoney(0.0, UiCurrencyCode.SEK),
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
        url = it.url,
      )
    },
  )
}

@HedvigPreview
@Composable
private fun PreviewAddonSummaryScreen(
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
        {},
      )
    }
  }
}

private class ChooseInsuranceForAddonUiStateProvider :
  CollectionPreviewParameterProvider<AddonSummaryState>(
    listOf(
      Loading(activationDateToNavigateToSuccess = null),
      Content(
        currentlyActiveAddons = listOf(
          CurrentlyActiveAddon(
            displayTitle = "Travel Plus 45 days",
            displayDescription = "description",
            cost = ItemCost(
              UiMoney(49.0, UiCurrencyCode.SEK),
              UiMoney(49.0, UiCurrencyCode.SEK),
              emptyList(),
            ),
          ),
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
                url = "",
              ),
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
            addonSubtype = "DAYS_60",
          ),
        ),
        navigateToFailure = null,
        insuranceExposure = "Exposure",
        notificationMessage = "Notification message",
        documents = emptyList(),
        costBreakdownWithExtras = CostBreakdownWithExtras(
          totalMonthlyNet = UiMoney(250.0, UiCurrencyCode.SEK),
          totalMonthlyGross = UiMoney(250.0, UiCurrencyCode.SEK),
          totalExtra = UiMoney(50.0, UiCurrencyCode.SEK),
          displayItems = listOf(
            CostBreakdownEntry(
              displayName = "base insurance",
              displayValue = "200 kr/mo",
              hasStrikethrough = false,
            ),
            CostBreakdownEntry(
              displayName = "addon",
              displayValue = "50 kr/mo",
              hasStrikethrough = false,
            ),
          ),
        ),
        displayItems = emptyList(),
        contractGroup = null,
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
                url = "",
              ),
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
            addonSubtype = "DAYS_60",
          ),
        ),
        navigateToFailure = null,
        insuranceExposure = "Exposure",
        notificationMessage = "Notification message",
        documents = emptyList(),
        costBreakdownWithExtras = CostBreakdownWithExtras(
          totalMonthlyNet = UiMoney(250.0, UiCurrencyCode.SEK),
          totalMonthlyGross = UiMoney(250.0, UiCurrencyCode.SEK),
          totalExtra = UiMoney(50.0, UiCurrencyCode.SEK),
          displayItems = listOf(
            CostBreakdownEntry(
              displayName = "base insurance",
              displayValue = "200 kr/mo",
              hasStrikethrough = false,
            ),
            CostBreakdownEntry(
              displayName = "addon",
              displayValue = "50 kr/mo",
              hasStrikethrough = false,
            ),
          ),
        ),
        displayItems = emptyList(),
        contractGroup = null,
      ),
    ),
  )
