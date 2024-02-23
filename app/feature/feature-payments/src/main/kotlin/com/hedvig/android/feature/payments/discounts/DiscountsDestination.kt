package com.hedvig.android.feature.payments.discounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.bottomsheet.HedvigInfoBottomSheet
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.HedvigIcons
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.feature.payments.overview.OverViewUiState
import com.hedvig.android.feature.payments.overview.PaymentEvent
import com.hedvig.android.feature.payments.overview.PaymentOverviewViewModel
import com.hedvig.android.feature.payments.paymentOverViewPreviewData
import hedvig.resources.R

@Composable
internal fun DiscountsDestination(viewModel: PaymentOverviewViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  DiscountsScreen(
    uiState = uiState,
    onDismissBottomSheet = {
      viewModel.emit(PaymentEvent.DismissBottomSheet)
    },
    onShowBottomSheet = {
      viewModel.emit(PaymentEvent.ShowBottomSheet)
    },
    onSubmitDiscountCode = {
      viewModel.emit(PaymentEvent.OnSubmitDiscountCode(it))
    },
    navigateUp = navigateUp,
  )
}

@Composable
internal fun DiscountsScreen(
  uiState: OverViewUiState,
  navigateUp: () -> Unit,
  onShowBottomSheet: () -> Unit,
  onDismissBottomSheet: () -> Unit,
  onSubmitDiscountCode: (String) -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(id = R.string.PAYMENTS_DISCOUNTS_SECTION_TITLE),
    navigateUp = navigateUp,
  ) {
    var showInfoBottomSheet by remember { mutableStateOf(false) }
    if (showInfoBottomSheet) {
      HedvigInfoBottomSheet(
        onDismissed = { showInfoBottomSheet = false },
        title = stringResource(id = R.string.PAYMENTS_CAMPAIGNS_INFO_TITLE),
        body = stringResource(id = R.string.PAYMENTS_CAMPAIGNS_INFO_DESCRIPTION),
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
        windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top),
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
          Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
              .fillMaxWidth(),
          ) {
            Box(
              modifier = Modifier
                .fillMaxHeight()
                .width(32.dp)
                .clip(MaterialTheme.shapes.squircleMedium)
                .clickable { showInfoBottomSheet = true },
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = HedvigIcons.InfoFilled,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = "Info icon",
                modifier = Modifier.size(16.dp),
              )
            }
          }
        },
      )

      val discounts = uiState.paymentOverview?.discounts
      if (discounts.isNullOrEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = stringResource(id = R.string.PAYMENTS_NO_CAMPAIGN_CODE_ADDED),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        discounts.forEachIndexed { index, discount ->
          DiscountRow(discount)
          if (index < discounts.size - 1) {
            HorizontalDivider()
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      HedvigSecondaryContainedButton(
        text = stringResource(id = R.string.PAYMENTS_ADD_CAMPAIGN_CODE),
        onClick = { onShowBottomSheet() },
      )
    }
  }
}

@Composable
@HedvigPreview
private fun PaymentDetailsScreenPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      DiscountsScreen(
        uiState = OverViewUiState(
          paymentOverViewPreviewData.copy(discounts = emptyList()),
        ),
        navigateUp = {},
        onShowBottomSheet = {},
        onDismissBottomSheet = {},
        onSubmitDiscountCode = {},
      )
    }
  }
}
