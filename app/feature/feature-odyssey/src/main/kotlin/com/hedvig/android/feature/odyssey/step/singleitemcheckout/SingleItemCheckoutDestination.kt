package com.hedvig.android.feature.odyssey.step.singleitemcheckout

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.designsystem.component.bottomsheet.HedvigBottomSheet
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import com.hedvig.android.core.ui.SelectIndicationCircle
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.scaffold.ClaimFlowScaffold
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.claimflow.CheckoutMethod
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import hedvig.resources.R
import octopus.type.CurrencyCode

@Composable
internal fun SingleItemCheckoutDestination(
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
private fun SingleItemCheckoutScreen(
  uiState: SingleItemCheckoutUiState.Content,
  windowSizeClass: WindowSizeClass,
  selectCheckoutMethod: (CheckoutMethod.Known) -> Unit,
  submitSelectedCheckoutMethod: (CheckoutMethod.Known) -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  var bottomSheetText by remember { mutableStateOf<Pair<String, String>?>(null) }
  val explanationSheetState = rememberModalBottomSheetState(true)
  val bottomSheetTextValue = bottomSheetText
  if (bottomSheetTextValue != null) {
    SingleItemCheckoutInfoBottomSheet(
      onDismiss = { bottomSheetText = null },
      sheetState = explanationSheetState,
      explanationTitleAndText = bottomSheetTextValue,
    )
  }

  ClaimFlowScaffold(
    topAppBarText = stringResource(R.string.claims_payout_payout_label),
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    HedvigCard(
      modifier = sideSpacingModifier,
    ) {
      Text(
        text = uiState.compensation.payoutAmount.toString(),
        style = MaterialTheme.typography.displayMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(6.dp),
      )
    }
    Spacer(Modifier.height(24.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = stringResource(R.string.CLAIMS_CHECKOUT_COUNT_TITLE),
            modifier = sideSpacingModifier,
          )
        }
      },
      spaceBetween = 8.dp,
      endSlot = {
        Row(
          horizontalArrangement =
            Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val explanationText = when (uiState.compensation) {
            is ClaimFlowDestination.Compensation.Known.RepairCompensation -> stringResource(
              id = R.string.CLAIMS_CHECKOUT_REPAIR_CALCULATION_TEXT,
            )

            is ClaimFlowDestination.Compensation.Known.ValueCompensation -> stringResource(
              id = R.string.CLAIMS_CHECKOUT_NO_REPAIR_CALCULATION_TEXT,
            )
          }
          val title = stringResource(R.string.CLAIMS_CHECKOUT_COUNT_TITLE)
          IconButton(
            onClick = {
              bottomSheetText = Pair(title, explanationText)
            },
            modifier = Modifier
              .size(40.dp)
              .padding(end = 8.dp),
          ) {
            Icon(
              imageVector = Icons.Hedvig.InfoFilled,
              contentDescription = stringResource(R.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
              modifier = Modifier.size(16.dp),
            )
          }
        }
      },
    )
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
      Column(sideSpacingModifier) {
        val pairs = when (uiState.compensation) {
          is ClaimFlowDestination.Compensation.Known.RepairCompensation -> listOf(
            stringResource(
              R.string.CLAIMS_CHECKOUT_REPAIR_TITLE,
              uiState.modelDisplayName,
            ) to uiState.compensation.repairCost.toString(),
            stringResource(R.string.claims_payout_age_deductable) to "-" + uiState.compensation.deductible.toString(),
          )

          is ClaimFlowDestination.Compensation.Known.ValueCompensation -> listOf(
            stringResource(R.string.KEY_GEAR_ITEM_VIEW_VALUATION_PAGE_TITLE) to uiState.compensation.price.toString(),
            stringResource(R.string.claims_payout_age_deduction) to "-" + uiState.compensation.depreciation.toString(),
            stringResource(R.string.claims_payout_age_deductable) to "-" + uiState.compensation.deductible.toString(),
          )
        }
        for ((left, right) in pairs) {
          HorizontalItemsWithMaximumSpaceTaken(
            startSlot = {
              Text(left)
            },
            spaceBetween = 8.dp,
            endSlot = {
              Text(right, textAlign = TextAlign.End)
            },
          )
        }
      }
    }
    Spacer(Modifier.height(16.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = stringResource(R.string.CLAIMS_PAYOUT_HEDVIG_LABEL),
            modifier = sideSpacingModifier,
          )
        }
      },
      endSlot = {
        Row(
          horizontalArrangement =
            Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = uiState.compensation.payoutAmount.toString(),
            modifier = Modifier.padding(end = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      },
    )
    Spacer(Modifier.height(16.dp))
    when (uiState.compensation) {
      is ClaimFlowDestination.Compensation.Known.RepairCompensation -> {
        VectorInfoCard(stringResource(R.string.CLAIMS_CHECKOUT_REPAIR_INFO_TEXT), sideSpacingModifier)
        Spacer(Modifier.height(16.dp))
      }

      is ClaimFlowDestination.Compensation.Known.ValueCompensation -> {
        HorizontalDivider(sideSpacingModifier, thickness = Dp.Hairline)
        Spacer(Modifier.height(16.dp))
      }
    }
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = stringResource(R.string.PAYMENTS_SUBTITLE_PAYMENT_METHOD),
            modifier = sideSpacingModifier,
          )
        }
      },
      endSlot = {
        Row(
          horizontalArrangement =
            Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val explanationText = stringResource(id = R.string.CLAIMS_CHECKOUT_PAYOUT_TEXT)
          val explanationTitle = stringResource(id = R.string.PAYMENTS_SUBTITLE_PAYMENT_METHOD)
          IconButton(
            onClick = {
              bottomSheetText = Pair(explanationTitle, explanationText)
            },
            modifier = Modifier
              .size(40.dp)
              .padding(end = 8.dp),
          ) {
            Icon(
              imageVector = Icons.Hedvig.InfoFilled,
              contentDescription = stringResource(R.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
              modifier = Modifier.size(16.dp),
            )
          }
        }
      },
    )
    CheckoutMethods(
      availableCheckoutMethods = uiState.availableCheckoutMethods,
      selectedCheckoutMethod = uiState.selectedCheckoutMethod,
      selectCheckoutMethod = selectCheckoutMethod,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.weight(1f))
    if (uiState.compensation is ClaimFlowDestination.Compensation.Known.ValueCompensation) {
      VectorInfoCard(stringResource(R.string.CLAIMS_CHECKOUT_NOTICE), sideSpacingModifier)
      Spacer(Modifier.height(16.dp))
    }
    HedvigContainedButton(
      onClick = { submitSelectedCheckoutMethod(uiState.selectedCheckoutMethod) },
      text = stringResource(R.string.claims_payout_button_label, uiState.compensation.payoutAmount.toString()),
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

@Composable
internal fun SingleItemCheckoutInfoBottomSheet(
  onDismiss: () -> Unit,
  sheetState: SheetState,
  explanationTitleAndText: Pair<String, String>,
) {
  HedvigBottomSheet(
    onDismissed = onDismiss,
    sheetState = sheetState,
    content = {
      Text(
        text = explanationTitleAndText.first,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
      )
      Spacer(Modifier.height(8.dp))
      Text(
        text = explanationTitleAndText.second,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
      )
    },
  )
}

@HedvigPreview
@Composable
private fun PreviewSingleItemCheckoutScreenWithRepair() {
  val checkoutNr1 = CheckoutMethod.Known.AutomaticAutogiro(
    "#1",
    "Fancy payment method",
    UiMoney(2499.0, CurrencyCode.SEK),
  )
  var selected: CheckoutMethod.Known by remember { mutableStateOf(checkoutNr1) }
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      SingleItemCheckoutScreen(
        uiState = SingleItemCheckoutUiState.Content(
          compensation = ClaimFlowDestination.Compensation.Known.RepairCompensation(
            repairCost = UiMoney(3999.0, CurrencyCode.SEK),
            deductible = UiMoney(1000.0, CurrencyCode.SEK),
            payoutAmount = UiMoney(2999.0, CurrencyCode.SEK),
          ),
          availableCheckoutMethods = buildList {
            add(checkoutNr1)
          }.toNonEmptyListOrNull()!!,
          selectedCheckoutMethod = selected,
          "IPhone 12",
        ),
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        selectCheckoutMethod = { selected = it },
        submitSelectedCheckoutMethod = {},
        navigateUp = {},
        closeClaimFlow = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewSingleItemCheckoutScreenValueCompensation(
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
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      SingleItemCheckoutScreen(
        SingleItemCheckoutUiState.Content(
          ClaimFlowDestination.Compensation.Known.ValueCompensation(
            price = UiMoney(3999.0, CurrencyCode.SEK),
            depreciation = UiMoney(500.0, CurrencyCode.SEK),
            deductible = UiMoney(1000.0, CurrencyCode.SEK),
            payoutAmount = UiMoney(2499.0, CurrencyCode.SEK),
          ),
          buildList {
            add(checkoutNr1)
            if (withMultiplePayoutMethods) {
              add(checkoutNr2)
            }
          }.toNonEmptyListOrNull()!!,
          selected,
          "IPhone 12",
        ),
        WindowSizeClass.calculateForPreview(),
        { selected = it },
        {},
        {},
        {},
      )
    }
  }
}
