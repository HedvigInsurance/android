package com.hedvig.android.feature.payments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.designsystem.animation.animateContentHeight
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Payments
import com.hedvig.android.core.icons.hedvig.normal.Waiting
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.hedvigSecondaryDateTimeFormatter
import com.hedvig.android.core.ui.preview.DoubleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.feature.payments.data.CampaignCode
import com.hedvig.android.placeholder.PlaceholderHighlight
import com.hedvig.android.placeholder.fade
import com.hedvig.android.placeholder.placeholder
import hedvig.resources.R
import kotlin.time.Duration.Companion.days
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import octopus.type.CurrencyCode

@Composable
internal fun PaymentDestination(
  viewModel: PaymentViewModel,
  onBackPressed: () -> Unit,
  onPaymentHistoryClicked: () -> Unit,
  onChangeBankAccount: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  PaymentScreen(
    uiState = uiState,
    navigateUp = onBackPressed,
    onChangeBankAccount = onChangeBankAccount,
    onAddDiscountCode = { viewModel.emit(PaymentEvent.SubmitNewDiscountCode(it)) },
    onDiscountCodeChanged = { viewModel.emit(PaymentEvent.EditDiscountCode(it)) },
    onPaymentHistoryClicked = onPaymentHistoryClicked,
    onRetry = { viewModel.emit(PaymentEvent.Retry) },
  )
}

@Composable
private fun PaymentScreen(
  uiState: PaymentUiState,
  navigateUp: () -> Unit,
  onChangeBankAccount: () -> Unit,
  onAddDiscountCode: (String) -> Unit,
  onDiscountCodeChanged: (String) -> Unit,
  onPaymentHistoryClicked: () -> Unit,
  onRetry: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(R.string.PROFILE_PAYMENT_TITLE),
    navigateUp = navigateUp,
    modifier = Modifier.clearFocusOnTap(),
  ) {
    if (uiState is PaymentUiState.Error) {
      Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
        HedvigErrorSection(retry = onRetry)
      }
    } else {
      Column {
        Spacer(Modifier.height(16.dp))
        PaymentAmountCard(
          uiState,
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .placeholder(uiState is PaymentUiState.Loading, highlight = PlaceholderHighlight.fade())
            .animateContentHeight(),
        )
        if (uiState is PaymentUiState.Content) {
          Spacer(Modifier.height(12.dp))
          if (uiState.nextChargeStatus is PaymentUiState.Content.NextChargeStatus.UpcomingCharge) {
            NextPayment(uiState.nextChargeStatus)
          }
          if (uiState.insuranceCosts.isNotEmpty()) {
            Divider(Modifier.padding(horizontal = 16.dp))
            InsuranceCosts(uiState.insuranceCosts)
          }
          if (uiState.monthlyCostDiscount != null) {
            Divider(Modifier.padding(horizontal = 16.dp))
            TotalDiscount(uiState.monthlyCostDiscount)
          }
          when (uiState.campaignsStatus) {
            is PaymentUiState.Content.CampaignsStatus.ExistingDiscounts -> {
              Spacer(Modifier.height(12.dp))
              for (activeDiscount in uiState.campaignsStatus.activeDiscounts) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(32.dp)
                    .padding(horizontal = 16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(activeDiscount.campaignCode.code)
                  Spacer(Modifier.width(8.dp))
                  Text(
                    text = activeDiscount.displayName,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
              Spacer(Modifier.height(12.dp))
              Divider(Modifier.padding(horizontal = 16.dp))
            }

            is PaymentUiState.Content.CampaignsStatus.AddNewCampaignCode -> {
              AddDiscount(
                campaignsStatus = uiState.campaignsStatus,
                onAddDiscountCode = onAddDiscountCode,
                onDiscountCodeChanged = onDiscountCodeChanged,
              )
              Divider(Modifier.padding(horizontal = 16.dp))
            }
          }
          MonthlyPayment(uiState.netMonthlyCost)
          Spacer(Modifier.height(32.dp))
          if (uiState.paymentDetails is PaymentUiState.Content.PaymentDetails.PaymentConnected) {
            PaymentDetails(uiState.paymentDetails)
            Divider(Modifier.padding(horizontal = 16.dp))
          }
          PaymentHistory(
            Modifier
              .clickable { onPaymentHistoryClicked() }
              .padding(16.dp),
          )
          Spacer(Modifier.height(16.dp))
          when (uiState.paymentDetails) {
            PaymentUiState.Content.PaymentDetails.NoPaymentConnected -> {
              HedvigContainedButton(
                text = stringResource(R.string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_BUTTON),
                onClick = onChangeBankAccount,
                modifier = Modifier.padding(horizontal = 16.dp),
              )
            }

            is PaymentUiState.Content.PaymentDetails.PaymentConnected -> {
              HedvigContainedButton(
                text = stringResource(R.string.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT),
                onClick = onChangeBankAccount,
                modifier = Modifier.padding(horizontal = 16.dp),
              )
            }

            PaymentUiState.Content.PaymentDetails.PaymentConnectionPending,
            PaymentUiState.Content.PaymentDetails.Unknown,
            -> {
            }
          }
          Spacer(Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
private fun PaymentAmountCard(uiState: PaymentUiState, modifier: Modifier = Modifier) {
  HedvigCard(modifier = modifier) {
    val nextChargeAmount = (uiState as? PaymentUiState.Content)?.nextChargeStatus?.nextCharge?.toString()
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = nextChargeAmount ?: "",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.displayMedium,
        modifier = Modifier.padding(vertical = 6.dp),
      )
      // placeholder so that this layout always takes as much space as the text above. This does not get rendered
      Text(
        text = "399,0 kr",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.displayMedium,
        modifier = Modifier
          .padding(vertical = 6.dp)
          .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {}
          },
      )
    }
  }
}

@Composable
private fun NextPayment(upcomingCharge: PaymentUiState.Content.NextChargeStatus.UpcomingCharge) {
  val locale = getLocale()
  val dateTimeFormatter = remember(locale) { hedvigSecondaryDateTimeFormatter(locale) }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(stringResource(R.string.PAYMENTS_NEXT_PAYMENT_SECTION_TITLE))
    Text(
      text = dateTimeFormatter.format(upcomingCharge.nextChargeDate.toJavaLocalDate()),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.InsuranceCosts(insuranceCosts: ImmutableList<PaymentUiState.Content.InsuranceCost>) {
  for ((index, insuranceCost) in insuranceCosts.withIndex()) {
    if (index != 0) {
      Divider(modifier = Modifier.padding(horizontal = 16.dp))
    }
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Image(
          painter = painterResource(insuranceCost.contractType.toPillow()),
          contentDescription = null,
          modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(insuranceCost.displayName)
      }
      Text(
        text = insuranceCost.cost.toString(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun TotalDiscount(monthlyCostDiscount: UiMoney) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(stringResource(R.string.PAYMENTS_SUBTITLE_DISCOUNT))
    Text(
      text = monthlyCostDiscount.toString(),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun ColumnScope.AddDiscount(
  campaignsStatus: PaymentUiState.Content.CampaignsStatus.AddNewCampaignCode,
  onAddDiscountCode: (String) -> Unit,
  onDiscountCodeChanged: (String) -> Unit,
) {
  var showDiscountInput by rememberSaveable { mutableStateOf(false) }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(stringResource(R.string.PAYMENTS_ADD_CODE_LABEL))
    Switch(
      checked = showDiscountInput,
      onCheckedChange = { showDiscountInput = !showDiscountInput },
      colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.typeElement),
    )
  }
  AnimatedVisibility(
    visible = showDiscountInput,
    enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Max)
        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      var textFieldInput by rememberSaveable { mutableStateOf(campaignsStatus.discountCodeInput) }
      HedvigTextField(
        value = textFieldInput,
        onValueChange = {
          textFieldInput = it
          onDiscountCodeChanged(it)
        },
        errorText = campaignsStatus.discountError?.let { discountError ->
          if (discountError.errorMessage?.message != null) {
            discountError.errorMessage.message
          } else {
            stringResource(id = R.string.general_error)
          }
        },
        enabled = !campaignsStatus.isSubmittingNewCampaignCode,
        label = {
          Text(stringResource(R.string.REFERRAL_ADDCOUPON_INPUTPLACEHOLDER))
        },
        keyboardActions = KeyboardActions(
          onDone = {
            onAddDiscountCode(campaignsStatus.discountCodeInput)
          },
        ),
        modifier = Modifier.weight(1f),
      )
      Spacer(Modifier.width(8.dp))
      HedvigContainedSmallButton(
        text = stringResource(R.string.PAYMENTS_ADD_CODE_BUTTON_LABEL),
        isLoading = campaignsStatus.isSubmittingNewCampaignCode,
        onClick = {
          onAddDiscountCode(campaignsStatus.discountCodeInput)
        },
        enabled = campaignsStatus.canSubmit,
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.typeContainer,
          contentColor = MaterialTheme.colorScheme.onTypeContainer,
        ),
      )
    }
  }
}

@Composable
private fun MonthlyPayment(netMonthlyCost: UiMoney) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(stringResource(R.string.payment_details_receipt_card_total))
    Text(
      text = stringResource(
        id = R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
        netMonthlyCost,
      ),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.PaymentDetails(paymentDetails: PaymentUiState.Content.PaymentDetails.PaymentConnected) {
  Text(
    text = stringResource(R.string.payment_details_navigation_bar_title),
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
  Divider(modifier = Modifier.padding(horizontal = 16.dp))
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Hedvig.Payments,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
      )
      Spacer(Modifier.width(16.dp))
      Text(paymentDetails.displayName)
    }
    Spacer(Modifier.width(8.dp))
    Text(
      text = paymentDetails.censoredPaymentDetails,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
fun PaymentHistory(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = Icons.Hedvig.Waiting,
      contentDescription = null,
      modifier = Modifier.size(24.dp),
    )
    Spacer(Modifier.width(16.dp))
    Text(stringResource(id = R.string.PAYMENTS_PAYMENT_HISTORY_BUTTON_LABEL))
  }
}

@Composable
@HedvigPreview
private fun PreviewPaymentScreen(
  @PreviewParameter(DoubleBooleanCollectionPreviewParameterProvider::class) input: Pair<Boolean, Boolean>,
) {
  val hasPaymentConnected = input.first
  val hasCampaigns = input.second
  HedvigTheme {
    Surface {
      PaymentScreen(
        uiState = PaymentUiState.Content(
          nextChargeStatus = PaymentUiState.Content.NextChargeStatus.UpcomingCharge(
            nextCharge = UiMoney(amount = 299.0, currencyCode = CurrencyCode.SEK),
            nextChargeDate = Clock.System.now().plus(1.days).toLocalDateTime(TimeZone.currentSystemDefault()).date,
          ),
          netMonthlyCost = UiMoney(amount = 199.0, currencyCode = CurrencyCode.SEK),
          monthlyCostDiscount = UiMoney(amount = 50.0, currencyCode = CurrencyCode.SEK),
          insuranceCosts = listOf(
            PaymentUiState.Content.InsuranceCost(
              displayName = "Home Insurance",
              cost = UiMoney(amount = 149.0, currencyCode = CurrencyCode.SEK),
              contractType = ContractType.HOUSE,
            ),
            PaymentUiState.Content.InsuranceCost(
              displayName = "Accident Insurance",
              cost = UiMoney(amount = 100.0, currencyCode = CurrencyCode.SEK),
              contractType = ContractType.ACCIDENT,
            ),
          ).toImmutableList(),
          campaignsStatus = if (hasCampaigns) {
            PaymentUiState.Content.CampaignsStatus.ExistingDiscounts(
              activeDiscounts = listOf(
                PaymentUiState.Content.ExistingDiscount(
                  campaignCode = CampaignCode(code = "HEDVIG"),
                  displayName = "-25kr/mån",
                ),
                PaymentUiState.Content.ExistingDiscount(
                  campaignCode = CampaignCode(code = "BANK"),
                  displayName = "-30kr/mån",
                ),
              ),
            )
          } else {
            PaymentUiState.Content.CampaignsStatus.AddNewCampaignCode(
              discountCodeInput = "123",
              discountError = PaymentUiState.Content.CampaignsStatus.AddNewCampaignCode.DiscountError(
                ErrorMessage("Invalid Code"),
              ),
              isSubmittingNewCampaignCode = true,
            )
          },
          paymentDetails = if (hasPaymentConnected) {
            PaymentUiState.Content.PaymentDetails.PaymentConnected(
              displayName = "Nordea",
              censoredPaymentDetails = "**** **** **** 3148",
            )
          } else {
            PaymentUiState.Content.PaymentDetails.NoPaymentConnected
          },
        ),
        {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
