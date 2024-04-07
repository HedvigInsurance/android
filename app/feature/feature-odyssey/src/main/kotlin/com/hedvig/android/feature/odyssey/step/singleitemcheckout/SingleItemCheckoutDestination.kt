package com.hedvig.android.feature.odyssey.step.singleitemcheckout

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.core.designsystem.HedvigPreviewLayout
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.ui.SelectIndicationCircle
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.scaffold.ClaimFlowScaffold
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.claimflow.CheckoutMethod
import hedvig.resources.R
import octopus.type.CurrencyCode

@Composable
internal fun SharedTransitionScope.SingleItemCheckoutDestination(
  animatedContentScope: AnimatedContentScope,
  viewModel: SingleItemCheckoutViewModel,
  windowSizeClass: WindowSizeClass,
  navigateToAppUpdateStep: () -> Unit,
  navigateToPayoutStep: (CheckoutMethod.Known) -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  when (val state = uiState) {
    is SingleItemCheckoutUiState.Unavailable -> {
      LaunchedEffect(state) {
        navigateToAppUpdateStep()
      }
      HedvigFullScreenCenterAlignedProgress()
    }

    is SingleItemCheckoutUiState.Content -> {
      SingleItemCheckoutScreen(
        animatedContentScope = animatedContentScope,
        uiState = state,
        windowSizeClass = windowSizeClass,
        selectCheckoutMethod = viewModel::selectCheckoutMethod,
        submitSelectedCheckoutMethod = navigateToPayoutStep,
        navigateUp = navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
  }
}

@Composable
private fun SharedTransitionScope.SingleItemCheckoutScreen(
  animatedContentScope: AnimatedContentScope,
  uiState: SingleItemCheckoutUiState.Content,
  windowSizeClass: WindowSizeClass,
  selectCheckoutMethod: (CheckoutMethod.Known) -> Unit,
  submitSelectedCheckoutMethod: (CheckoutMethod.Known) -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  ClaimFlowScaffold(
    animatedContentScope = animatedContentScope,
    topAppBarText = stringResource(R.string.claims_payout_payout_label),
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(R.string.claims_payout_summary_subtitle),
      style = MaterialTheme.typography.bodyLarge,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigCard(
      modifier = sideSpacingModifier,
    ) {
      Text(
        text = uiState.payoutAmount.toString(),
        style = MaterialTheme.typography.displayMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
      )
    }
    Spacer(Modifier.height(24.dp))
    Text(
      text = stringResource(R.string.CLAIMS_CHECKOUT_COUNT_TITLE),
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(8.dp))
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
      Column(sideSpacingModifier) {
        val pairs = listOf(
          stringResource(R.string.KEY_GEAR_ITEM_VIEW_VALUATION_PAGE_TITLE) to uiState.price.toString(),
          stringResource(R.string.claims_payout_age_deduction) to "-" + uiState.depreciation.toString(),
          stringResource(R.string.claims_payout_age_deductable) to "-" + uiState.deductible.toString(),
        )
        for ((left, right) in pairs) {
          Row {
            Text(left, Modifier.weight(1f))
            Text(right, Modifier.weight(1f), textAlign = TextAlign.End)
          }
        }
      }
    }
    Spacer(Modifier.height(16.dp))
    HorizontalDivider(sideSpacingModifier)
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(R.string.PAYMENTS_SUBTITLE_PAYMENT_METHOD),
      modifier = sideSpacingModifier,
    )
    CheckoutMethods(
      availableCheckoutMethods = uiState.availableCheckoutMethods,
      selectedCheckoutMethod = uiState.selectedCheckoutMethod,
      selectCheckoutMethod = selectCheckoutMethod,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.weight(1f))
    VectorInfoCard(stringResource(R.string.CLAIMS_CHECKOUT_NOTICE), sideSpacingModifier)
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      onClick = { submitSelectedCheckoutMethod(uiState.selectedCheckoutMethod) },
      text = stringResource(R.string.claims_payout_button_label, uiState.payoutAmount.toString()),
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.CheckoutMethods(
  availableCheckoutMethods: NonEmptyList<CheckoutMethod.Known>,
  selectedCheckoutMethod: CheckoutMethod.Known,
  selectCheckoutMethod: (CheckoutMethod.Known) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  val allowSelectingCheckoutMethod = availableCheckoutMethods.size > 1
  for (checkoutMethod in availableCheckoutMethods) {
    val isSelected = checkoutMethod == selectedCheckoutMethod
    Spacer(Modifier.height(8.dp))
    HedvigCard(
      onClick = if (allowSelectingCheckoutMethod) {
        { selectCheckoutMethod(checkoutMethod) }
      } else {
        null
      },
      enabled = if (allowSelectingCheckoutMethod) {
        enabled
      } else {
        true
      },
      modifier = modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = checkoutMethod.displayName,
          style = MaterialTheme.typography.headlineSmall,
          modifier = Modifier.weight(1f),
        )
        if (allowSelectingCheckoutMethod) {
          Spacer(Modifier.width(16.dp))
          SelectIndicationCircle(selected = isSelected)
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewSingleItemCheckoutScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) withMultiplePayoutMethods: Boolean,
) {
  val checkoutNr1 = CheckoutMethod.Known.AutomaticAutogiro(
    "#1",
    "Autogiro".repeat(4),
    UiMoney(2499.0, CurrencyCode.SEK),
  )
  val checkoutNr2 = CheckoutMethod.Known.AutomaticAutogiro(
    "#2",
    "Bankenbanken",
    UiMoney(2499.0, CurrencyCode.SEK),
  )
  var selected: CheckoutMethod.Known by remember { mutableStateOf(checkoutNr1) }
  HedvigPreviewLayout { animatedContentScope ->
    SingleItemCheckoutScreen(
      animatedContentScope,
      SingleItemCheckoutUiState.Content(
        UiMoney(3999.0, CurrencyCode.SEK),
        UiMoney(500.0, CurrencyCode.SEK),
        UiMoney(1000.0, CurrencyCode.SEK),
        UiMoney(2499.0, CurrencyCode.SEK),
        buildList {
          add(checkoutNr1)
          if (withMultiplePayoutMethods) {
            add(checkoutNr2)
          }
        }.toNonEmptyListOrNull()!!,
        selected,
      ),
      WindowSizeClass.calculateForPreview(),
      { selected = it },
      {},
      {},
      {},
    )
  }
}
