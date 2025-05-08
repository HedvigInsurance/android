package com.hedvig.android.feature.payments.ui.discounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.minimumInteractiveComponentSize
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.overview.data.ForeverInformation
import com.hedvig.android.feature.payments.overview.data.ReferredByInfo
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun DiscountsDestination(
  viewModel: DiscountsViewModel,
  navigateUp: () -> Unit,
  navigateToForever: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  DiscountsScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    navigateToForever = navigateToForever,
    retry = { viewModel.emit(DiscountsEvent.Retry) },
  )
}

@Composable
private fun DiscountsScreen(
  uiState: DiscountsUiState,
  navigateUp: () -> Unit,
  retry: () -> Unit,
  navigateToForever: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(R.string.PAYMENTS_DISCOUNTS_SECTION_TITLE),
    navigateUp = navigateUp,
  ) {
    if (uiState.isLoadingPaymentOverView) {
      HedvigFullScreenCenterAlignedProgress(Modifier.weight(1f))
    } else if (uiState.error != null) {
      HedvigErrorSection(
        modifier = Modifier.weight(1f),
        onButtonClick = retry,
        buttonText = stringResource(R.string.GENERAL_RETRY),
      )
    } else {
      Column(
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .verticalScroll(rememberScrollState())
          .weight(1f),
      ) {
        val discounts = uiState.discounts
        if (!discounts.isEmpty()) {
          Spacer(modifier = Modifier.height(24.dp))
          DiscountRows(discounts, showDisplayName = true)
          Spacer(modifier = Modifier.height(16.dp))
        }
        if (uiState.foreverInformation != null) {
          Spacer(modifier = Modifier.height(24.dp))
          ForeverSection(uiState.foreverInformation)
          Spacer(modifier = Modifier.height(16.dp))
          Spacer(Modifier.weight(1f))
          HedvigNotificationCard(
            message = stringResource(R.string.PAYMENTS_REFERRALS_INFO_DESCRIPTION),
            priority = NotificationDefaults.NotificationPriority.Campaign,
            style = NotificationDefaults.InfoCardStyle.Button(
              buttonText = stringResource(R.string.important_message_read_more),
              onButtonClick = navigateToForever,
            ),
          )
          Spacer(modifier = Modifier.height(16.dp))
        } else {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
private fun ForeverSection(foreverInformation: ForeverInformation, modifier: Modifier = Modifier) {
  Column(modifier.fillMaxHeight()) {
    var showForeverInfoBottomSheet by remember { mutableStateOf(false) }
    ForeverExplanationBottomSheet(
      showForeverInfoBottomSheet = showForeverInfoBottomSheet,
      onClose = {
        showForeverInfoBottomSheet = false
      },
    )
    HorizontalItemsWithMaximumSpaceTaken(
      spaceBetween = 8.dp,
      startSlot = {
        HedvigText(
          stringResource(R.string.PAYMENTS_REFERRALS_INFO_TITLE),
          modifier = Modifier.wrapContentSize(Alignment.CenterStart),
        )
      },
      endSlot = {
        Icon(
          imageVector = HedvigIcons.InfoFilled,
          tint = HedvigTheme.colorScheme.fillSecondary,
          contentDescription = null,
          modifier = Modifier
            .wrapContentSize(Alignment.CenterEnd)
            .size(24.dp)
            .clip(HedvigTheme.shapes.cornerXLarge)
            .clickable { showForeverInfoBottomSheet = true }
            .minimumInteractiveComponentSize(),
        )
      },
    )
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))
    if (foreverInformation.referredBy != null) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Column {
            Row {
              HighlightLabel(
                labelText =
                  foreverInformation.referredBy.code?.uppercase() ?: foreverInformation.referredBy.name.uppercase(),
                size = HighlightLabelDefaults.HighLightSize.Small,
                modifier = Modifier
                  .wrapContentWidth(),
                color = HighlightLabelDefaults.HighlightColor.Grey(HighlightLabelDefaults.HighlightShade.LIGHT),
              )
            }
            HedvigText(
              stringResource(
                R.string.FOREVER_REFERRAL_INVITED_YOU,
                foreverInformation.referredBy.name,
              ),
              color = HedvigTheme.colorScheme.textSecondaryTranslucent,
              fontSize = HedvigTheme.typography.label.fontSize,
            )
          }
        },
        endSlot = {
          Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.End,
          ) {
            HedvigText(
              stringResource(
                R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                "-${foreverInformation.referredBy.activeDiscount}",
              ),
              color = HedvigTheme.colorScheme.textSecondaryTranslucent,
              textAlign = TextAlign.End,
            )
          }
        },
        spaceBetween = 8.dp,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider()
      Spacer(modifier = Modifier.height(16.dp))
    }
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Column {
          Row {
            HighlightLabel(
              labelText = foreverInformation.foreverCode,
              size = HighlightLabelDefaults.HighLightSize.Small,
              modifier = Modifier
                .wrapContentWidth(),
              color = HighlightLabelDefaults.HighlightColor.Grey(HighlightLabelDefaults.HighlightShade.LIGHT),
            )
          }
          HedvigText(
            pluralStringResource(
              R.plurals.FOREVER_REFERRAL_INVITED_BY_YOU_PLURAL,
              foreverInformation.numberOfReferrals,
              foreverInformation.numberOfReferrals,
            ),
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            fontSize = HedvigTheme.typography.label.fontSize,
          )
        }
      },
      endSlot = {
        Row(
          verticalAlignment = Alignment.Top,
          horizontalArrangement = Arrangement.End,
        ) {
          HedvigText(
            stringResource(
              R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              "-${foreverInformation.currentMonthlyDiscountFromReferrals}",
            ),
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            textAlign = TextAlign.End,
          )
        }
      },
      spaceBetween = 8.dp,
    )
  }
}

@Composable
internal fun ForeverExplanationBottomSheet(showForeverInfoBottomSheet: Boolean, onClose: () -> Unit) {
  HedvigBottomSheet(
    isVisible = showForeverInfoBottomSheet,
    onVisibleChange = { visible ->
      if (!visible) {
        onClose()
      }
    },
  ) {
    HedvigText(text = stringResource(R.string.referrals_info_sheet_headline))
    HedvigText(
      text = stringResource(R.string.PAYMENTS_REFERRALS_INFO_DESCRIPTION),
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.general_close_button),
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
      onClick = onClose,
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
@HedvigPreview
private fun PaymentDetailsScreenPreview(
  @PreviewParameter(
    BooleanCollectionPreviewParameterProvider::class,
  ) isLoading: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DiscountsScreen(
        uiState = DiscountsUiState(
          discounts =
            listOf(
              Discount(
                "MYDISCOUNT1",
                "display name of referral",
                "description",
                Discount.ExpiredState.NotExpired,
                UiMoney(10.0, UiCurrencyCode.SEK),
                false,
              ),
              Discount(
                "MYDISCOUNT2",
                "display name of non referral",
                "description",
                Discount.ExpiredState.NotExpired,
                UiMoney(10.0, UiCurrencyCode.SEK),
                false,
              ),
              Discount(
                "MYDISCOUNT3",
                "display name of expiring soon soon soon soon soon",
                "description",
                Discount.ExpiredState.ExpiringInTheFuture(LocalDate(2124, 12, 14)),
                UiMoney(10.0, UiCurrencyCode.SEK),
                false,
              ),
              Discount(
                "MYDISCOUNT3",
                "display name of expired",
                "description",
                Discount.ExpiredState.AlreadyExpired(LocalDate(2014, 12, 14)),
                UiMoney(10.0, UiCurrencyCode.SEK),
                false,
              ),
            ),
          isLoadingPaymentOverView = isLoading,
          foreverInformation = ForeverInformation(
            "MYDISCOUNT1",
            UiMoney(20.0, UiCurrencyCode.SEK),
            UiMoney(10.0, UiCurrencyCode.SEK),
            numberOfReferrals = 2,
            referredBy = ReferredByInfo(
              name = "Sladan",
              code = "UBJSOS",
              activeDiscount = UiMoney(
                10.0,
                UiCurrencyCode.SEK,
              ),
            ),
          ),
        ),
        navigateUp = {},
        retry = {},
        navigateToForever = {},
      )
    }
  }
}
