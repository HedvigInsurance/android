package com.hedvig.android.feature.payments.discounts

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.designsystem.component.bottomsheet.HedvigInfoBottomSheet
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.onSecondaryContainedButtonContainer
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.secondaryContainedButtonContainer
import com.hedvig.android.core.designsystem.material3.squircleLarge
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import com.hedvig.android.core.designsystem.material3.squircleSmall
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import com.hedvig.android.core.icons.hedvig.small.hedvig.Campaign
import com.hedvig.android.core.ui.infocard.InfoCardTextButton
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
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
    if (showInfoBottomSheet) {
      HedvigInfoBottomSheet(
        onDismissed = { showInfoBottomSheet = false },
        title = stringResource(R.string.PAYMENTS_CAMPAIGNS_INFO_TITLE),
        body = stringResource(R.string.PAYMENTS_CAMPAIGNS_INFO_DESCRIPTION),
      )
    }
    if (uiState.showAddDiscountBottomSheet) {
      val sheetState = rememberModalBottomSheetState(true)
      ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismissBottomSheet,
        shape = MaterialTheme.shapes.squircleLargeTop,
        sheetState = sheetState,
        tonalElevation = 0.dp,
        contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top) },
      ) {
        AddDiscountBottomSheet(
          onAddDiscount = { code ->
            onSubmitDiscountCode(code)
          },
          errorMessage = uiState.discountError,
          isLoading = uiState.isAddingDiscount,
        )
      }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
      Spacer(modifier = Modifier.height(16.dp))
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Text(stringResource(id = R.string.PAYMENTS_CAMPAIGNS_INFO_TITLE))
        },
        endSlot = {
          Icon(
            imageVector = Icons.Hedvig.InfoFilled,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = "Info icon",
            modifier = Modifier
              .wrapContentSize(Alignment.CenterEnd)
              .size(16.dp)
              .clip(MaterialTheme.shapes.squircleLarge)
              .clickable { showInfoBottomSheet = true }
              .minimumInteractiveComponentSize(),
          )
        },
      )

      val discounts = uiState.discounts
      if (discounts.isEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = stringResource(id = R.string.PAYMENTS_NO_CAMPAIGN_CODE_ADDED),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        Spacer(modifier = Modifier.height(16.dp))
        DiscountRows(discounts)
      }

      Spacer(modifier = Modifier.height(16.dp))
      HedvigSecondaryContainedButton(
        text = stringResource(id = R.string.PAYMENTS_ADD_CAMPAIGN_CODE),
        onClick = { onShowBottomSheet() },
      )
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
    val incentive = foreverInformation.potentialDiscountAmountPerNewReferral.toString()
    var showForeverInfoBottomSheet by remember { mutableStateOf(false) }
    if (showForeverInfoBottomSheet) {
      HedvigInfoBottomSheet(
        onDismissed = { showForeverInfoBottomSheet = false },
        title = stringResource(R.string.referrals_info_sheet_headline),
        body = stringResource(R.string.referrals_info_sheet_body, incentive),
      )
    }
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Text(
          stringResource(R.string.PAYMENTS_REFERRALS_INFO_TITLE),
          modifier = Modifier.wrapContentSize(Alignment.CenterStart),
        )
      },
      endSlot = {
        Icon(
          imageVector = Icons.Hedvig.InfoFilled,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          contentDescription = null,
          modifier = Modifier
            .wrapContentSize(Alignment.CenterEnd)
            .size(16.dp)
            .clip(MaterialTheme.shapes.squircleLarge)
            .clickable { showForeverInfoBottomSheet = true }
            .minimumInteractiveComponentSize(),
        )
      },
    )
    Spacer(modifier = Modifier.height(16.dp))
    val context = LocalContext.current
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigCard(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainedButtonContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainedButtonContainer,
          ),
          shape = MaterialTheme.shapes.squircleSmall,
          onClick = {
            context.getSystemService<ClipboardManager>()?.setPrimaryClip(
              ClipData.newPlainText(null, foreverInformation.foreverCode),
            )
          },
          modifier = Modifier.wrapContentSize(Alignment.TopStart),
        ) {
          Text(
            text = foreverInformation.foreverCode,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      },
      endSlot = {
        Text(
          stringResource(
            R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
            "-${foreverInformation.currentMonthlyDiscountFromForever}",
          ),
          textAlign = TextAlign.End,
        )
      },
      spaceBetween = 8.dp,
    )
    Spacer(modifier = Modifier.height(16.dp))
    VectorInfoCard(
      text = stringResource(R.string.PAYMENTS_REFERRALS_INFO_DESCRIPTION),
      iconColor = MaterialTheme.colorScheme.typeElement,
      icon = Icons.Hedvig.Campaign,
      colors = CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.typeContainer,
        contentColor = MaterialTheme.colorScheme.onTypeContainer,
      ),
      underTextContent = {
        InfoCardTextButton(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.important_message_read_more),
          onClick = navigateToForever,
        )
      },
    )
  }
}

@Composable
@HedvigPreview
private fun PaymentDetailsScreenPreview(
  @PreviewParameter(
    com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider::class,
  ) hasForeverAndDiscounts: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
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
                "display name of expiring soon",
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
