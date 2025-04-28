package com.hedvig.android.feature.payments.ui.discounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.minimumInteractiveComponentSize
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.overview.data.ForeverInformation
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
    onDismissBottomSheet = { viewModel.emit(DiscountsEvent.DismissBottomSheet) },
    onShowBottomSheet = { viewModel.emit(DiscountsEvent.ShowBottomSheet) },
    onSubmitDiscountCode = { viewModel.emit(DiscountsEvent.OnSubmitDiscountCode(it)) },
    navigateUp = navigateUp,
    navigateToForever = navigateToForever,
  )
}

@Composable
private fun DiscountsScreen(
  uiState: DiscountsUiState,
  navigateUp: () -> Unit,
  onShowBottomSheet: () -> Unit,
  onDismissBottomSheet: () -> Unit,
  onSubmitDiscountCode: (String) -> Unit,
  navigateToForever: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(R.string.PAYMENTS_DISCOUNTS_SECTION_TITLE),
    navigateUp = navigateUp,
  ) {
    var showInfoBottomSheet by remember { mutableStateOf(false) }
    HedvigBottomSheet(
      isVisible = showInfoBottomSheet,
      onVisibleChange = { visible ->
        if (!visible) {
          showInfoBottomSheet = false
        }
      },
    ) {
      HedvigText(text = stringResource(R.string.PAYMENTS_CAMPAIGNS_INFO_TITLE))
      HedvigText(
        text = stringResource(R.string.PAYMENTS_CAMPAIGNS_INFO_DESCRIPTION),
        color = HedvigTheme.colorScheme.textSecondary,
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(id = R.string.general_close_button),
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
        onClick = {
          showInfoBottomSheet = false
        },
      )
      Spacer(Modifier.height(8.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
    HedvigBottomSheet(
      isVisible = uiState.showAddDiscountBottomSheet,
      onVisibleChange = { visible ->
        if (!visible) {
          onDismissBottomSheet()
        }
      },
    ) {
      AddDiscountBottomSheetContent(
        onAddDiscount = { code ->
          onSubmitDiscountCode(code)
        },
        errorMessage = uiState.discountError?.let { discountError ->
          discountError.message ?: stringResource(R.string.something_went_wrong)
        },
        isLoading = uiState.isAddingDiscount,
        onDismiss = onDismissBottomSheet,
      )
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
      Spacer(modifier = Modifier.height(16.dp))
      HorizontalItemsWithMaximumSpaceTaken(
        spaceBetween = 8.dp,
        startSlot = {
          HedvigText(stringResource(id = R.string.PAYMENTS_CAMPAIGNS_INFO_TITLE))
        },
        endSlot = {
          if (uiState.allowAddingCampaignCode) {
            Icon(
              imageVector = HedvigIcons.InfoFilled,
              tint = HedvigTheme.colorScheme.fillSecondary,
              contentDescription = null,
              modifier = Modifier
                .wrapContentSize(Alignment.CenterEnd)
                .size(16.dp)
                .clip(HedvigTheme.shapes.cornerXLarge)
                .clickable { showInfoBottomSheet = true }
                .minimumInteractiveComponentSize(),
            )
          }
        },
      )

      val discounts = uiState.discounts
      if (discounts.isEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        HedvigText(
          text = stringResource(id = R.string.PAYMENTS_NO_CAMPAIGN_CODE_ADDED),
          color = HedvigTheme.colorScheme.textSecondary,
        )
      } else {
        Spacer(modifier = Modifier.height(16.dp))
        DiscountRows(discounts, showDisplayName = true)
      }

      if (uiState.allowAddingCampaignCode) {
        Spacer(modifier = Modifier.height(16.dp))
        HedvigButton(
          buttonStyle = ButtonStyle.Secondary,
          modifier = Modifier.fillMaxWidth(),
          enabled = true,
          text = stringResource(id = R.string.PAYMENTS_ADD_CAMPAIGN_CODE),
          onClick = { onShowBottomSheet() },
        )
      }
      if (uiState.foreverInformation != null) {
        Spacer(modifier = Modifier.height(32.dp))
        ForeverSection(uiState.foreverInformation, navigateToForever, Modifier)
      }
      Spacer(modifier = Modifier.height(16.dp))
      Spacer(
        modifier = Modifier.padding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues()),
      )
    }
  }
}

@Composable
private fun ForeverSection(
  foreverInformation: ForeverInformation,
  navigateToForever: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
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
    Spacer(modifier = Modifier.height(8.dp))
    val context = LocalContext.current
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
            pluralStringResource(R.plurals.FOREVER_REFERRAL_INVITED_BY_YOU_PLURAL, 2,2),
            //todo: put right args!
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            fontSize = HedvigTheme.typography.label.fontSize
          )
        }

      },
      endSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.End,
        ) {
          HedvigText(
            stringResource(
              R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              "-${foreverInformation.currentMonthlyDiscountFromForever}",
            ),
            textAlign = TextAlign.End,
          )
        }
      },
      spaceBetween = 8.dp,
    )
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
  ) hasForeverAndDiscounts: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DiscountsScreen(
        uiState = DiscountsUiState(
          discounts = if (hasForeverAndDiscounts) {
            listOf(
              Discount(
                "MYDISCOUNT1",
                "display name of referral",
                "description",
                Discount.ExpiredState.NotExpired,
                UiMoney(10.0, UiCurrencyCode.SEK),
                true,
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
            )
          } else {
            emptyList()
          },
          foreverInformation = ForeverInformation(
            "MYDISCOUNT1",
            UiMoney(23.0, UiCurrencyCode.SEK),
            UiMoney(10.0, UiCurrencyCode.SEK),
          ).takeIf { hasForeverAndDiscounts },
          allowAddingCampaignCode = true,
        ),
        navigateUp = {},
        onShowBottomSheet = {},
        onDismissBottomSheet = {},
        onSubmitDiscountCode = {},
        navigateToForever = {},
      )
    }
  }
}
