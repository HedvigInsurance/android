package com.hedvig.android.feature.odyssey.step.singleitemcheckout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.nonEmptyListOf
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.UiMoney
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.feature.odyssey.data.ClaimFlowStep
import com.hedvig.android.feature.odyssey.navigation.CheckoutMethod
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
import hedvig.resources.R
import octopus.type.CurrencyCode

@Composable
internal fun SingleItemCheckoutDestination(
  viewModel: SingleItemCheckoutViewModel,
  windowSizeClass: WindowSizeClass,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateToAppUpdateStep: () -> Unit,
  navigateBack: () -> Unit,
  openChat: () -> Unit,
  exitFlow: () -> Unit,
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
        uiState = state,
        windowSizeClass = windowSizeClass,
        selectCheckoutMethod = viewModel::selectCheckoutMethod,
        onDoneAfterPayout = navigateToNextStep,
        submitSelections = viewModel::requestPayout,
        navigateBack = navigateBack,
        openChat = openChat,
        exitFlow = exitFlow,
      )
    }
  }
}

@Composable
private fun SingleItemCheckoutScreen(
  uiState: SingleItemCheckoutUiState.Content,
  windowSizeClass: WindowSizeClass,
  selectCheckoutMethod: (CheckoutMethod.Known) -> Unit,
  onDoneAfterPayout: (ClaimFlowStep) -> Unit,
  submitSelections: () -> Unit,
  navigateBack: () -> Unit,
  openChat: () -> Unit,
  exitFlow: () -> Unit,
) {
  Box {
    ClaimFlowScaffold(
      topAppBarText = stringResource(R.string.claims_payout_payout_label),
      windowSizeClass = windowSizeClass,
      navigateUp = navigateBack,
    ) { sideSpacingModifier ->
      Spacer(Modifier.height(16.dp))
      Text(
        text = stringResource(R.string.claims_payout_summary_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        modifier = sideSpacingModifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigCard(
        shape = MaterialTheme.shapes.squircle,
        modifier = sideSpacingModifier,
      ) {
        Text(
          text = uiState.payoutAmount.toString(),
          style = MaterialTheme.typography.displayMedium,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(6.dp),
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
      Divider(sideSpacingModifier)
      if (uiState.availableCheckoutMethods.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text(
          text = stringResource(R.string.PAYMENTS_SUBTITLE_PAYMENT_METHOD),
          modifier = sideSpacingModifier,
        )
        CheckoutMethods(
          availableCheckoutMethods = uiState.availableCheckoutMethods,
          selectedCheckoutMethod = uiState.selectedCheckoutMethod,
          selectCheckoutMethod = selectCheckoutMethod,
          enabled = uiState.canRequestPayout,
          modifier = sideSpacingModifier,
        )
      }
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.weight(1f))
      VectorInfoCard(stringResource(R.string.CLAIMS_CHECKOUT_NOTICE), sideSpacingModifier)
      Spacer(Modifier.height(16.dp))
      HedvigContainedButton(
        onClick = submitSelections,
        enabled = uiState.canRequestPayout,
        text = stringResource(R.string.claims_payout_button_label, "· ${uiState.payoutAmount}"),
        modifier = sideSpacingModifier,
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
    }
    AnimatedVisibility(
      visible = uiState.payoutUiState.shouldRender,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      PayoutScreen(
        uiState = uiState.payoutUiState,
        exitFlow = exitFlow,
        onDoneAfterPayout = onDoneAfterPayout,
        retryPayout = submitSelections,
        openChat = openChat,
      )
    }
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.CheckoutMethods(
  availableCheckoutMethods: List<CheckoutMethod.Known>,
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
      shape = MaterialTheme.shapes.squircle,
      modifier = modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = checkoutMethod.displayName,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
          style = MaterialTheme.typography.headlineSmall,
          modifier = Modifier.weight(1f),
        )
        if (isSelected && allowSelectingCheckoutMethod) {
          Spacer(Modifier.width(16.dp))
          Icon(Icons.Default.CheckCircle, null)
        }
      }
    }
  }
}

@Preview(locale = "sv")
@HedvigPreview
@Composable
private fun PreviewSingleItemCheckoutScreen() {
  val checkoutNr1 = CheckoutMethod.Known.AutomaticAutogiro(
    "#1",
    "Autogiro".repeat(10),
    UiMoney(2499.0, CurrencyCode.SEK),
  )
  val checkoutNr2 = CheckoutMethod.Known.AutomaticAutogiro(
    "#2",
    "Handelsbanken".repeat(3),
    UiMoney(2499.0, CurrencyCode.SEK),
  )
  var selected: CheckoutMethod.Known by remember { mutableStateOf(checkoutNr1) }
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      SingleItemCheckoutScreen(
        SingleItemCheckoutUiState.Content(
          UiMoney(3999.0, CurrencyCode.SEK),
          UiMoney(500.0, CurrencyCode.SEK),
          UiMoney(1000.0, CurrencyCode.SEK),
          UiMoney(2499.0, CurrencyCode.SEK),
          nonEmptyListOf(
            checkoutNr1,
            checkoutNr2,
          ),
          selected,
        ),
        WindowSizeClass.calculateForPreview(),
        { selected = it },
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
