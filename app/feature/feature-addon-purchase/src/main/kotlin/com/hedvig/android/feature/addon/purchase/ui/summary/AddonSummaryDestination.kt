package com.hedvig.android.feature.addon.purchase.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.DialogDefaults
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.ripple
import com.hedvig.android.feature.addon.purchase.data.CurrentTravelAddon
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Content
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Loading
import com.hedvig.android.tiersandaddons.QuoteCardDocumentsSection
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

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
    Loading -> HedvigFullScreenCenterAlignedProgress()

    is Content -> {
      LaunchedEffect(uiState.navigateToFailure) {
        val fail = uiState.navigateToFailure
        if (fail) {
          onFailure()
        }
      }
      LaunchedEffect(uiState.activationDateForSuccessfullyPurchasedAddon) {
        val date = uiState.activationDateForSuccessfullyPurchasedAddon
        if (date != null) {
          onSuccess(date)
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
    topAppBarText = stringResource(R.string.TIER_FLOW_SUMMARY_TITLE),
  ) {
    val locale = getLocale()
    val formattedDate = remember(uiState.activationDate, locale) {
      HedvigDateTimeFormatterDefaults.dateMonthAndYear(
        locale,
      ).format(uiState.activationDate.toJavaLocalDate())
    }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    if (showConfirmationDialog) {
      HedvigAlertDialog(
        title = stringResource(R.string.ADDON_FLOW_CONFIRMATION_TITLE),
        onDismissRequest = { showConfirmationDialog = false },
        onConfirmClick = onConfirmClick,
        buttonSize = DialogDefaults.ButtonSize.BIG,
        confirmButtonLabel = stringResource(R.string.ADDON_FLOW_CONFIRMATION_BUTTON),
        dismissButtonLabel = stringResource(R.string.general_close_button),
        text = stringResource(
          R.string.ADDON_FLOW_CONFIRMATION_DESCRIPTION,
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
      HedvigNotificationCard(
        message = stringResource(R.string.ADDON_FLOW_SUMMARY_INFO_TEXT),
        priority = NotificationDefaults.NotificationPriority.Info,
      )
      Spacer(Modifier.height(24.dp))
      HorizontalItemsWithMaximumSpaceTaken(
        modifier = Modifier.semantics(true) {},
        startSlot = {
          HedvigText(
            stringResource(R.string.TIER_FLOW_TOTAL),
            style = HedvigTheme.typography.bodySmall,
          )
        },
        spaceBetween = 8.dp,
        endSlot = {
          val text = if (uiState.totalPriceChange.amount > 0) {
            // with +
            stringResource(
              R.string.ADDON_FLOW_PRICE_LABEL,
              uiState.totalPriceChange,
            )
          } else {
            // without + (supposedly with minus)
            stringResource(
              R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              uiState.totalPriceChange,
            )
          }
          val voiceDescription = uiState.totalPriceChange.getPerMonthDescription()
          HedvigText(
            text = text,
            textAlign = TextAlign.End,
            style = HedvigTheme.typography.bodySmall,
            modifier = Modifier.semantics(true) {
              contentDescription = voiceDescription
            },
          )
        },
      )
      Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
      ) {
        HedvigText(
          text = stringResource(R.string.ADDON_FLOW_SUMMARY_PRICE_SUBTITLE),
          textAlign = TextAlign.End,
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(R.string.ADDON_FLOW_SUMMARY_CONFIRM_BUTTON),
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
    ).format(uiState.activationDate.toJavaLocalDate())
  }
  HedvigCard(
    modifier = modifier,
    onClick = null,
    interactionSource = null,
    indication = ripple(bounded = true, radius = 1000.dp),
  ) {
    Column(Modifier.padding(16.dp)) {
      HedvigText(
        text = uiState.offerDisplayName,
        overflow = TextOverflow.Ellipsis,
      )
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HedvigText(
          text = stringResource(R.string.ADDON_FLOW_SUMMARY_ACTIVE_FROM, formattedDate),
          color = HedvigTheme.colorScheme.textSecondary,
          overflow = TextOverflow.Ellipsis,
        )
        HorizontalItemsWithMaximumSpaceTaken(
          startSlot = {
            HedvigText(stringResource(R.string.TIER_FLOW_TOTAL))
          },
          endSlot = {
            val newPricePerMonth = uiState.quote.price.getPerMonthDescription()
            val newPriceDescription = stringResource(
              R.string.TALKBACK_YOUR_NEW_PRICE,
              newPricePerMonth,
            )
            Row(
              horizontalArrangement = Arrangement.End,
              modifier = Modifier.semantics(true) {},
            ) {
              if (uiState.currentTravelAddon != null) {
                val previousPricePerMonth = uiState.currentTravelAddon.price.getPerMonthDescription()
                val previousPriceDescription = stringResource(
                  R.string.TIER_FLOW_PREVIOUS_PRICE,
                  previousPricePerMonth,
                )
                HedvigText(
                  stringResource(
                    R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                    uiState.currentTravelAddon.price,
                  ),
                  style = HedvigTheme.typography.bodySmall.copy(
                    textDecoration = TextDecoration.LineThrough,
                    color = HedvigTheme.colorScheme.textSecondary,
                  ),
                  modifier = Modifier.semantics {
                    contentDescription = previousPriceDescription
                  },
                )
                Spacer(Modifier.width(8.dp))
                HedvigText(
                  stringResource(
                    R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                    uiState.quote.price,
                  ),
                  modifier = Modifier.semantics {
                    contentDescription = newPriceDescription
                  },
                )
              } else {
                HedvigText(
                  stringResource(
                    R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                    uiState.quote.price,
                  ),
                  modifier = Modifier.semantics {
                    contentDescription = newPricePerMonth
                  },
                )
              }
            }
          },
          spaceBetween = 8.dp,
        )
        HorizontalDivider()
        if (uiState.quote.displayDetails.isNotEmpty()) {
          Column(
            Modifier.semantics(true) {},
          ) {
            HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_OVERVIEW_SUBTITLE))
            DetailsWithStrikeThrough(uiState)
          }
        }
        if (uiState.quote.addonVariant.documents.isNotEmpty()) {
          QuoteCardDocumentsSection(uiState.quote.addonVariant.documents)
        }
      }
    }
  }
}

@Composable
private fun DetailsWithStrikeThrough(uiState: Content) {
  uiState.quote.displayDetails.forEach { quoteItem ->
    val currentAddonValue = uiState.currentTravelAddon?.displayDetails?.firstOrNull { currentAddonItem ->
      currentAddonItem.first == quoteItem.first
    }?.second
    val valueToStrikeThrough = if (currentAddonValue != null &&
      currentAddonValue != quoteItem.second
    ) {
      currentAddonValue
    } else {
      null
    }
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigText(quoteItem.first, color = HedvigTheme.colorScheme.textSecondary)
      },
      endSlot = {
        Row(horizontalArrangement = Arrangement.End) {
          if (valueToStrikeThrough != null) {
            val strikeThroughDescription = stringResource(R.string.TALKBACK_PREVIOUS_VALUE, valueToStrikeThrough)
            HedvigText(
              valueToStrikeThrough,
              style = HedvigTheme.typography.bodySmall.copy(
                textDecoration = TextDecoration.LineThrough,
                color = HedvigTheme.colorScheme.textSecondary,
              ),
              modifier = Modifier.semantics {
                contentDescription = strikeThroughDescription
              },
            )
            Spacer(Modifier.width(8.dp))
          }
          val newValueDescription = if (valueToStrikeThrough != null) {
            stringResource(R.string.TALKBACK_NEW_VALUE, quoteItem.second)
          } else {
            quoteItem.second
          }
          HedvigText(
            quoteItem.second,
            color = HedvigTheme.colorScheme.textSecondary,
            modifier = Modifier.semantics {
              contentDescription = newValueDescription
            },
          )
        }
      },
      spaceBetween = 8.dp,
    )
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
      Loading,
      Content(
        currentTravelAddon = CurrentTravelAddon(
          UiMoney(49.0, UiCurrencyCode.SEK),
          listOf("Coverage" to "45 days", "Insured people" to "You+1"),
        ),
        offerDisplayName = "TravelPlus",
        activationDate = LocalDate(2025, 1, 1),
        quote = TravelAddonQuote(
          displayName = "60 days",
          addonId = "addonId1",
          quoteId = "id",
          displayDetails = listOf(
            "Amount of insured people" to "You +1",
            "Coverage" to "60 days",
          ),
          addonVariant = AddonVariant(
            termsVersion = "terms",
            documents = listOf(),
            perils = listOf(),
            displayName = "45 days",
            product = "",
          ),
          addonSubtype = "45 days",
          price = UiMoney(
            60.0,
            UiCurrencyCode.SEK,
          ),
        ),
        activationDateForSuccessfullyPurchasedAddon = null,
        navigateToFailure = false,
        totalPriceChange = UiMoney(11.0, UiCurrencyCode.SEK),
      ),
    ),
  )
