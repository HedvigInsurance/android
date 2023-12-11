package com.hedvig.android.feature.payments2.discounts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.payments2.DiscountRow
import com.hedvig.android.feature.payments2.OverViewUiState
import com.hedvig.android.feature.payments2.PaymentEvent
import com.hedvig.android.feature.payments2.PaymentOverviewViewModel
import com.hedvig.android.feature.payments2.paymentOverViewPreviewData
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
      Text(stringResource(id = R.string.PAYMENTS_CAMPAIGNS_INFO_TITLE))

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
            Divider()
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
