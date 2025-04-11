package com.hedvig.android.feature.odyssey.step.singleitemcheckout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.claimflow.CheckoutMethod
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowDestination.SingleItemCheckout.Compensation.Known.RepairCompensation
import com.hedvig.android.data.claimflow.ClaimFlowDestination.SingleItemCheckout.Compensation.Known.ValueCompensation
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.LockedState.Locked
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize.Small
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataSimple
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.a11y.getDescription
import com.hedvig.android.design.system.hedvig.calculateForPreview
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import hedvig.resources.R

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
  val hedvigBottomSheetState = rememberHedvigBottomSheetState<Pair<String, String>>()
  SingleItemCheckoutInfoBottomSheet(hedvigBottomSheetState)

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
      val payoutVoiceover = uiState.compensation.payoutAmount.getDescription()
      HedvigText(
        text = uiState.compensation.payoutAmount.toString(),
        style = HedvigTheme.typography.displaySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(6.dp)
          .semantics {
            contentDescription = payoutVoiceover
          },
      )
    }
    Spacer(Modifier.height(24.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          HedvigText(
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
            is RepairCompensation -> stringResource(
              id = R.string.CLAIMS_CHECKOUT_REPAIR_CALCULATION_TEXT,
            )

            is ValueCompensation -> stringResource(
              id = R.string.CLAIMS_CHECKOUT_NO_REPAIR_CALCULATION_TEXT,
            )
          }
          val title = stringResource(R.string.CLAIMS_CHECKOUT_COUNT_TITLE)
          IconButton(
            onClick = {
              hedvigBottomSheetState.show(Pair(title, explanationText))
            },
            modifier = Modifier
              .size(40.dp)
              .padding(end = 8.dp),
          ) {
            Icon(
              imageVector = HedvigIcons.InfoFilled,
              contentDescription = stringResource(R.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
              modifier = Modifier.size(16.dp),
            )
          }
        }
      },
    )
    CompositionLocalProvider(LocalContentColor provides HedvigTheme.colorScheme.textSecondary) {
      Column(sideSpacingModifier) {
        val pairs = when (uiState.compensation) {
          is RepairCompensation -> listOf(
            RepairPair(
              stringResource(
                R.string.CLAIMS_CHECKOUT_REPAIR_TITLE,
                uiState.modelDisplayName,
              ),
              uiState.compensation.repairCost.toString(),
              uiState.compensation.repairCost,
            ),
            RepairPair(
              stringResource(R.string.claims_payout_age_deductable),
              "-" + uiState.compensation.deductible.toString(),
              uiState.compensation.deductible,
            ),
          )

          is ValueCompensation -> listOf(
            RepairPair(
              stringResource(R.string.KEY_GEAR_ITEM_VIEW_VALUATION_PAGE_TITLE),
              uiState.compensation.price.toString(),
              uiState.compensation.price,
            ),
            RepairPair(
              stringResource(R.string.claims_payout_age_deduction),
              "-" + uiState.compensation.depreciation.toString(),
              uiState.compensation.depreciation,
            ),
            RepairPair(
              stringResource(R.string.claims_payout_age_deductable),
              "-" + uiState.compensation.deductible.toString(),
              uiState.compensation.deductible,
            ),
          )
        }
        for (pair in pairs) {
          val voiceDescription = pair.getVoiceDescription()
          HorizontalItemsWithMaximumSpaceTaken(
            startSlot = {
              HedvigText(pair.label)
            },
            spaceBetween = 8.dp,
            endSlot = {
              HedvigText(pair.amountText, textAlign = TextAlign.End)
            },
            modifier = Modifier.clearAndSetSemantics {
              contentDescription = voiceDescription
            },
          )
        }
      }
    }
    Spacer(Modifier.height(16.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      spaceBetween = 8.dp,
      startSlot = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          HedvigText(
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
          val voiceoverDescription = uiState.compensation.payoutAmount.getDescription()
          HedvigText(
            text = uiState.compensation.payoutAmount.toString(),
            modifier = Modifier
              .padding(end = 16.dp)
              .semantics {
                contentDescription = voiceoverDescription
              },
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      },
    )
    Spacer(Modifier.height(16.dp))
    when (uiState.compensation) {
      is RepairCompensation -> {
        HedvigNotificationCard(
          stringResource(R.string.CLAIMS_CHECKOUT_REPAIR_INFO_TEXT),
          NotificationPriority.Info,
          sideSpacingModifier,
        )
        Spacer(Modifier.height(16.dp))
      }

      is ValueCompensation -> {
        HorizontalDivider(sideSpacingModifier, thickness = Dp.Hairline)
        Spacer(Modifier.height(16.dp))
      }
    }
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          HedvigText(
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
              hedvigBottomSheetState.show(Pair(explanationTitle, explanationText))
            },
            modifier = Modifier
              .size(40.dp)
              .padding(end = 8.dp),
          ) {
            Icon(
              imageVector = HedvigIcons.InfoFilled,
              contentDescription = stringResource(R.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
              modifier = Modifier.size(16.dp),
            )
          }
        }
      },
      spaceBetween = 8.dp,
    )
    CheckoutMethods(
      availableCheckoutMethods = uiState.availableCheckoutMethods,
      selectedCheckoutMethod = uiState.selectedCheckoutMethod,
      selectCheckoutMethod = selectCheckoutMethod,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.weight(1f))
    if (uiState.compensation is ValueCompensation) {
      HedvigNotificationCard(
        stringResource(R.string.CLAIMS_CHECKOUT_NOTICE),
        NotificationPriority.Info,
        sideSpacingModifier,
      )
      Spacer(Modifier.height(16.dp))
    }
    val payoutDescription =
      stringResource(R.string.claims_payout_button_label, uiState.compensation.payoutAmount.getDescription())
    HedvigButton(
      text = stringResource(R.string.claims_payout_button_label, uiState.compensation.payoutAmount.toString()),
      onClick = { submitSelectedCheckoutMethod(uiState.selectedCheckoutMethod) },
      enabled = true,
      modifier = sideSpacingModifier
        .fillMaxWidth()
        .clearAndSetSemantics {
          contentDescription = payoutDescription
          role = Role.Button
        },
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

private data class RepairPair(
  val label: String,
  val amountText: String,
  val amountMoney: UiMoney,
)

@Composable
private fun RepairPair.getVoiceDescription(): String {
  val amountDescription = this.amountMoney.getDescription()
  return "${this.label}, $amountDescription"
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun CheckoutMethods(
  availableCheckoutMethods: NonEmptyList<CheckoutMethod.Known>,
  selectedCheckoutMethod: CheckoutMethod.Known,
  selectCheckoutMethod: (CheckoutMethod.Known) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  Column(modifier) {
    val allowSelectingCheckoutMethod = availableCheckoutMethods.size > 1
    Spacer(Modifier.height(8.dp))
    if (allowSelectingCheckoutMethod) {
      RadioGroup(
        radioGroupStyle = RadioGroupStyle.Vertical.Default(
          availableCheckoutMethods.map { checkoutMethod ->
            RadioOptionGroupDataSimple(
              RadioOptionData(
                checkoutMethod.id,
                checkoutMethod.displayName,
                if (checkoutMethod == selectedCheckoutMethod) Chosen else NotChosen,
              ),
            )
          },
        ),
        radioGroupSize = Small,
        onOptionClick = { id ->
          selectCheckoutMethod(availableCheckoutMethods.first { it.id == id })
        },
        groupLockedState = if (enabled) NotLocked else Locked,
        modifier = Modifier.fillMaxWidth(),
      )
    } else {
      HedvigCard(Modifier.fillMaxWidth()) {
        Box(
          modifier = Modifier.padding(16.dp),
          propagateMinConstraints = true,
        ) {
          HedvigText(availableCheckoutMethods.head.displayName)
        }
      }
    }
  }
}

@Composable
internal fun SingleItemCheckoutInfoBottomSheet(sheetState: HedvigBottomSheetState<Pair<String, String>>) {
  HedvigBottomSheet(
    hedvigBottomSheetState = sheetState,
    content = { explanationTitleAndText ->
      HedvigText(
        text = explanationTitleAndText.first,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
      )
      Spacer(Modifier.height(8.dp))
      HedvigText(
        text = explanationTitleAndText.second,
        color = HedvigTheme.colorScheme.textSecondary,
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
    UiMoney(2499.0, UiCurrencyCode.SEK),
  )
  var selected: CheckoutMethod.Known by remember { mutableStateOf(checkoutNr1) }
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SingleItemCheckoutScreen(
        uiState = SingleItemCheckoutUiState.Content(
          compensation = ClaimFlowDestination.SingleItemCheckout.Compensation.Known.RepairCompensation(
            repairCost = UiMoney(3999.0, UiCurrencyCode.SEK),
            deductible = UiMoney(1000.0, UiCurrencyCode.SEK),
            payoutAmount = UiMoney(2999.0, UiCurrencyCode.SEK),
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
    UiMoney(2499.0, UiCurrencyCode.SEK),
  )
  val checkoutNr2 = CheckoutMethod.Known.AutomaticAutogiro(
    "#2",
    "Bankenbanken",
    UiMoney(2499.0, UiCurrencyCode.SEK),
  )
  var selected: CheckoutMethod.Known by remember { mutableStateOf(checkoutNr1) }
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SingleItemCheckoutScreen(
        SingleItemCheckoutUiState.Content(
          ClaimFlowDestination.SingleItemCheckout.Compensation.Known.ValueCompensation(
            price = UiMoney(3999.0, UiCurrencyCode.SEK),
            depreciation = UiMoney(500.0, UiCurrencyCode.SEK),
            deductible = UiMoney(1000.0, UiCurrencyCode.SEK),
            payoutAmount = UiMoney(2499.0, UiCurrencyCode.SEK),
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
