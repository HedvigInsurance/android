package com.hedvig.android.feature.payin.account.ui.methoddetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigRedTextButton
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.feature.payin.account.data.InvoiceDelivery
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.data.toDeliveryString
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodRow
import com.hedvig.android.feature.payin.account.ui.components.PrimaryMethodLabel
import com.hedvig.android.feature.payin.account.ui.components.maskedAccountNumber
import hedvig.resources.GENERAL_REMOVE
import hedvig.resources.KIVRA_PAYMENT_INFO
import hedvig.resources.PAYMENTS_ACCOUNT
import hedvig.resources.PAYMENTS_AUTOGIRO_LABEL
import hedvig.resources.PAYMENTS_BANK_LABEL
import hedvig.resources.PAYMENTS_DUE_DESCRIPTION
import hedvig.resources.PAYMENTS_INVOICE
import hedvig.resources.PAYMENTS_PAYMENT_DUE
import hedvig.resources.PAYMENTS_PAYMENT_DUE_INFO
import hedvig.resources.PAYMENTS_PAYMENT_METHOD
import hedvig.resources.PAYMENTS_SWISH_NUMBER
import hedvig.resources.PAYMENT_SWISH_CHANGE_NUMBER
import hedvig.resources.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT
import hedvig.resources.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION
import hedvig.resources.Res
import hedvig.resources.general_close_button
import hedvig.resources.something_went_wrong
import hedvig.resources.swish
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PayinMethodDetailsDestination(
  viewModel: PayinMethodDetailsViewModel,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  onChangeMethod: (PayinAccount) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val hasRemovedMethod = (uiState as? PayinMethodDetailsUiState.Content)?.hasRemovedMethod == true
  LaunchedEffect(hasRemovedMethod) {
    if (hasRemovedMethod) navigateBack()
  }
  when (val state = uiState) {
    PayinMethodDetailsUiState.Loading -> {
      HedvigScaffold(
        topAppBarText = stringResource(Res.string.PAYMENTS_PAYMENT_METHOD),
        navigateUp = navigateUp,
        modifier = Modifier.fillMaxSize(),
      ) {
        HedvigFullScreenCenterAlignedProgress(Modifier.weight(1f))
      }
    }

    PayinMethodDetailsUiState.Error -> {
      HedvigScaffold(
        topAppBarText = stringResource(Res.string.PAYMENTS_PAYMENT_METHOD),
        navigateUp = navigateUp,
        modifier = Modifier.fillMaxSize(),
      ) {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(PayinMethodDetailsEvent.Retry) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .weight(1f),
        )
      }
    }

    is PayinMethodDetailsUiState.Content -> {
      PayinMethodDetailsScreen(
        method = state.method,
        chargingDay = state.chargingDay,
        isRemoving = state.isRemoving,
        removeErrorMessage = state.removeError?.let {
          it.message ?: stringResource(Res.string.something_went_wrong)
        },
        navigateUp = navigateUp,
        onChangeMethod = { onChangeMethod(state.method) },
        onConfirmRemoveMethod = { viewModel.emit(PayinMethodDetailsEvent.RemoveMethod) },
      )
    }
  }
}

@Composable
private fun PayinMethodDetailsScreen(
  method: PayinAccount,
  chargingDay: Int?,
  isRemoving: Boolean,
  removeErrorMessage: String?,
  navigateUp: () -> Unit,
  onChangeMethod: () -> Unit,
  onConfirmRemoveMethod: () -> Unit,
) {
  val explanationSheetState = rememberHedvigBottomSheetState<PaymentDueExplanation>()
  ExplanationBottomSheet(explanationSheetState)
  val removeSheetState = rememberHedvigBottomSheetState<PayinAccount>()
  RemovePayinMethodBottomSheet(
    sheetState = removeSheetState,
    isRemoving = isRemoving,
    errorMessage = removeErrorMessage,
    onConfirmRemove = onConfirmRemoveMethod,
  )
  HedvigScaffold(
    topAppBarText = stringResource(Res.string.PAYMENTS_PAYMENT_METHOD),
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    HedvigCard(
      shape = HedvigTheme.shapes.cornerLarge,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
      PayinMethodRow(
        method = method,
        modifier = Modifier.fillMaxWidth(),
        endSlot = { if (method.isDefault) PrimaryMethodLabel() },
      )
    }
    Spacer(Modifier.height(8.dp))
    DetailRow(
      label = stringResource(Res.string.PAYMENTS_PAYMENT_METHOD),
      value = when (method) {
        is PayinAccount.Trustly -> stringResource(Res.string.PAYMENTS_AUTOGIRO_LABEL)
        is PayinAccount.SwishPayin -> stringResource(Res.string.swish)
        is PayinAccount.Invoice -> stringResource(Res.string.PAYMENTS_INVOICE)
      },
    )
    if (chargingDay != null) {
      val formattedDay = chargingDay.format()
      DetailRow(
        label = stringResource(Res.string.PAYMENTS_PAYMENT_DUE),
        value = stringResource(Res.string.PAYMENTS_DUE_DESCRIPTION, formattedDay),
        endSlot = {
          val explanation = method.explanation(formattedDay)
          if (explanation != null) {
            IconButton(
              onClick = { explanationSheetState.show(explanation) },
              modifier = Modifier.size(24.dp),
            ) {
              Icon(
                imageVector = HedvigIcons.InfoFilled,
                contentDescription = stringResource(Res.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
                modifier = Modifier.size(24.dp),
                tint = HedvigTheme.colorScheme.fillSecondaryTransparent,
              )
            }
          }
        },
      )
    }
    when (method) {
      is PayinAccount.SwishPayin -> {
        DetailRow(
          label = stringResource(Res.string.PAYMENTS_SWISH_NUMBER),
          value = method.phoneNumber.orEmpty(),
        )
      }

      is PayinAccount.Trustly -> {
        DetailRow(
          label = stringResource(Res.string.PAYMENTS_ACCOUNT),
          value = method.maskedAccountNumber().orEmpty(),
        )
        DetailRow(
          label = stringResource(Res.string.PAYMENTS_BANK_LABEL),
          value = method.bankName.orEmpty(),
        )
      }

      is PayinAccount.Invoice -> {
        DetailRow(
          label = stringResource(Res.string.PAYMENTS_ACCOUNT),
          value = method.email ?: method.delivery.toDeliveryString().orEmpty(),
        )
      }
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    val changeButtonText = when (method) {
      is PayinAccount.SwishPayin -> stringResource(Res.string.PAYMENT_SWISH_CHANGE_NUMBER)
      is PayinAccount.Trustly -> stringResource(Res.string.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT)
      is PayinAccount.Invoice -> null
    }
    if (changeButtonText != null) {
      HedvigButton(
        text = changeButtonText,
        onClick = onChangeMethod,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(8.dp))
    }
    HedvigRedTextButton(
      text = stringResource(Res.string.GENERAL_REMOVE),
      onClick = { removeSheetState.show(method) },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun DetailRow(label: String, value: String, endSlot: @Composable (() -> Unit)? = null) {
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = { HedvigText(label) },
    endSlot = {
      Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        HedvigText(
          text = value,
          textAlign = TextAlign.End,
          modifier = Modifier.weight(1f, fill = false),
          color = HedvigTheme.colorScheme.textSecondary,
        )
        if (endSlot != null) {
          Spacer(Modifier.width(8.dp))
          endSlot()
        }
      }
    },
    spaceBetween = 8.dp,
    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
  )
  HorizontalDivider(Modifier.padding(horizontal = 18.dp))
}

private sealed interface PaymentDueExplanation {
  data object Kivra : PaymentDueExplanation

  data class DirectDebit(val dueDate: String) : PaymentDueExplanation
}

private fun PayinAccount.explanation(formattedDay: String): PaymentDueExplanation? = when {
  this is PayinAccount.Trustly -> PaymentDueExplanation.DirectDebit(formattedDay)
  this is PayinAccount.Invoice && delivery == InvoiceDelivery.Kivra -> PaymentDueExplanation.Kivra
  else -> null
}

@Composable
private fun ExplanationBottomSheet(sheetState: HedvigBottomSheetState<PaymentDueExplanation>) {
  HedvigBottomSheet(sheetState) { explanation ->
    HedvigText(
      text = when (explanation) {
        PaymentDueExplanation.Kivra -> {
          stringResource(Res.string.KIVRA_PAYMENT_INFO)
        }

        is PaymentDueExplanation.DirectDebit -> {
          stringResource(Res.string.PAYMENTS_PAYMENT_DUE_INFO, explanation.dueDate)
        }
      },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_close_button),
      onClick = { sheetState.dismiss() },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun Int.format(): String {
  val day = this
  val lastDigit = day % 10

  val locale = getLocale()

  val suffix = when (locale.language) {
    "en" -> when (lastDigit) {
      1 -> "st"
      2 -> "nd"
      3 -> "rd"
      else -> "th"
    }

    "sv" -> when (day) {
      11, 12 -> ":e"

      else -> when (lastDigit) {
        1, 2 -> ":a"
        else -> ":e"
      }
    }

    else -> when (lastDigit) {
      1 -> "st"
      2 -> "nd"
      3 -> "rd"
      else -> "th"
    }
  }

  return "$day$suffix"
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewPayinMethodDetailsDestination(
  @PreviewParameter(PayinMethodPreviewProvider::class) method: PayinAccount,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PayinMethodDetailsScreen(
        method = method,
        chargingDay = 27,
        isRemoving = false,
        removeErrorMessage = null,
        navigateUp = {},
        onChangeMethod = {},
        onConfirmRemoveMethod = {},
      )
    }
  }
}

private class PayinMethodPreviewProvider : CollectionPreviewParameterProvider<PayinAccount>(
  listOf(
    PayinAccount.SwishPayin("0709901232", isPending = false, isDefault = true),
    PayinAccount.Trustly("8327", "91234124", "Swedbank", isPending = false, isDefault = false),
    PayinAccount.Invoice(InvoiceDelivery.Kivra, null, isPending = false, isDefault = false),
  ),
)
