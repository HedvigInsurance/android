package com.hedvig.android.odyssey.step.singleitemcheckout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.nonEmptyListOf
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.progress.FullScreenHedvigProgress
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.core.ui.text.HorizontalTextsWithMaximumSpaceTaken
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.navigation.CheckoutMethod
import com.hedvig.android.odyssey.navigation.UiGuaranteedMoney
import com.hedvig.android.odyssey.ui.ClaimFlowScaffold
import hedvig.resources.R
import octopus.type.CurrencyCode

@Composable
internal fun SingleItemCheckoutDestination(
  viewModel: SingleItemCheckoutViewModel,
  windowSizeClass: WindowSizeClass,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateToAppUpdateStep: () -> Unit,
  navigateBack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = uiState.asContent()?.nextStep
  LaunchedEffect(claimFlowStep) {
    if (claimFlowStep != null) {
      navigateToNextStep(claimFlowStep)
    }
  }
  when (val state = uiState) {
    is SingleItemCheckoutUiState.Unavailable -> {
      LaunchedEffect(state) {
        navigateToAppUpdateStep()
      }
      FullScreenHedvigProgress()
    }
    is SingleItemCheckoutUiState.Content -> {
      SingleItemCheckoutScreen(
        uiState = state,
        windowSizeClass = windowSizeClass,
        submitSelections = viewModel::requestPayout,
        showedError = viewModel::showedError,
        navigateBack = navigateBack,
      )
    }
  }
}

@Composable
private fun SingleItemCheckoutScreen(
  uiState: SingleItemCheckoutUiState.Content,
  windowSizeClass: WindowSizeClass,
  submitSelections: () -> Unit,
  showedError: () -> Unit,
  navigateBack: () -> Unit,
) {
  Box {
    ClaimFlowScaffold(
      windowSizeClass = windowSizeClass,
      navigateBack = navigateBack,
      errorSnackbarState = ErrorSnackbarState(uiState.hasError, showedError),
    ) { sideSpacingModifier ->
      Box(
        contentAlignment = Alignment.BottomStart,
        modifier = sideSpacingModifier
          .heightIn(80.dp)
          .padding(vertical = 16.dp),
      ) {
        Text(
          text = stringResource(R.string.claims_payout_summary_subtitle),
          style = MaterialTheme.typography.titleLarge,
        )
      }
      CompensationBreakdown(
        uiState.price,
        uiState.depreciation,
        uiState.deductible,
        uiState.payoutAmount,
        sideSpacingModifier,
      )
      CheckoutMethods(uiState.availableCheckoutMethods, sideSpacingModifier)
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.weight(1f))
      LargeContainedTextButton(
        onClick = submitSelections,
        enabled = uiState.canSubmit,
        text = stringResource(R.string.claims_payout_button_label, "· ${uiState.payoutAmount}"),
        modifier = sideSpacingModifier,
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
    }
    if (uiState.isLoading) {
//      PayoutProgress(
//        title = stringResource(R.string.claims_payout_progress_title),
//        onSuccessTitle = resolution.payoutAmount.amount ?: "", // TODO
//        onSuccessMessage = stringResource(R.string.claims_payout_success_message),
//        onContinueMessage = stringResource(R.string.claims_payout_done_label),
//        onContinue = onFinish,
//        isCompleted = isCompleted,
//      )
    }
  }
}

@Composable
private fun CompensationBreakdown(
  price: UiGuaranteedMoney,
  depreciation: UiGuaranteedMoney,
  deductible: UiGuaranteedMoney,
  payoutAmount: UiGuaranteedMoney,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    CheckoutRowItem(stringResource(R.string.claims_payout_purchase_price), price.toString())
    Divider()
    CheckoutRowItem(stringResource(R.string.claims_payout_age_deduction), depreciation.toString())
    Divider()
    CheckoutRowItem(stringResource(R.string.claims_payout_age_deductable), deductible.toString())
    Divider()
    CheckoutRowItem(stringResource(R.string.claims_payout_total), payoutAmount.toString(), true)
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.CheckoutMethods(
  availableCheckoutMethods: List<CheckoutMethod.Known>,
  modifier: Modifier = Modifier,
) {
  if (availableCheckoutMethods.isNotEmpty()) {
    Box(
      contentAlignment = Alignment.BottomStart,
      modifier = modifier
        .heightIn(80.dp)
        .padding(vertical = 16.dp),
    ) {
      Text(
        text = stringResource(R.string.claims_payout_summary_method),
        style = MaterialTheme.typography.titleLarge,
      )
    }
    for (checkoutMethod in availableCheckoutMethods) {
      Spacer(Modifier.height(8.dp))
      HedvigCard(
        modifier
          .fillMaxWidth()
          .heightIn(64.dp),
      ) {
        Box(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
          contentAlignment = Alignment.CenterStart,
        ) {
          Text(checkoutMethod.displayName)
        }
      }
    }
  }
}

@Composable
private fun CheckoutRowItem(
  startText: String,
  endText: String,
  withHighEmphasis: Boolean = false,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .heightIn(56.dp)
      .fillMaxWidth(),
  ) {
    HorizontalTextsWithMaximumSpaceTaken(
      startText = { Text(startText) },
      endText = { textAlign ->
        Text(
          text = endText,
          fontWeight = if (withHighEmphasis) FontWeight.Bold else LocalTextStyle.current.fontWeight,
          textAlign = textAlign,
        )
      },
      spaceBetween = 8.dp,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewSingleItemCheckoutScreen() {
  HedvigTheme {
    Surface {
      SingleItemCheckoutScreen(
        SingleItemCheckoutUiState.Content(
          UiGuaranteedMoney(3999.0, CurrencyCode.SEK),
          UiGuaranteedMoney(500.0, CurrencyCode.SEK),
          UiGuaranteedMoney(1000.0, CurrencyCode.SEK),
          UiGuaranteedMoney(2499.0, CurrencyCode.SEK),
          nonEmptyListOf(
            CheckoutMethod.Known.AutomaticAutogiro("", "Autogiro", UiGuaranteedMoney(2499.0, CurrencyCode.SEK)),
          ),
          CheckoutMethod.Known.AutomaticAutogiro("", "Autogiro", UiGuaranteedMoney(2499.0, CurrencyCode.SEK)),
        ),
        WindowSizeClass.calculateForPreview(),
        {},
        {},
        {},
      )
    }
  }
}
