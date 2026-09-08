package com.hedvig.android.feature.payin.account.ui.methoddetails

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigRedTextButton
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.payin.account.data.InvoiceDelivery
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.data.toDeliveryString
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodRow
import com.hedvig.android.feature.payin.account.ui.components.PrimaryMethodLabel
import com.hedvig.android.feature.payin.account.ui.components.maskedAccountNumber
import hedvig.resources.GENERAL_REMOVE
import hedvig.resources.PAYMENTS_ACCOUNT
import hedvig.resources.PAYMENTS_AUTOGIRO_LABEL
import hedvig.resources.PAYMENTS_BANK_LABEL
import hedvig.resources.PAYMENTS_INVOICE
import hedvig.resources.PAYMENTS_PAYMENT_METHOD
import hedvig.resources.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT
import hedvig.resources.Res
import hedvig.resources.swish
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PayinMethodDetailsDestination(
  viewModel: PayinMethodDetailsViewModel,
  navigateUp: () -> Unit,
  onChangeMethod: (PayinAccount) -> Unit,
  onRemoveMethod: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        navigateUp = navigateUp,
        onChangeMethod = { onChangeMethod(state.method) },
        onRemoveMethod = onRemoveMethod,
      )
    }
  }
}

@Composable
private fun PayinMethodDetailsScreen(
  method: PayinAccount,
  navigateUp: () -> Unit,
  onChangeMethod: () -> Unit,
  onRemoveMethod: () -> Unit,
) {
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
    // The charging day comes from MemberPaymentMethods.chargingDay, which the payin methods query does not
    // request yet. Restore this row, and the info-button explanation sheet that belongs to it, once it does.
    // DetailRow(
    //   label = stringResource(Res.string.PAYMENTS_PAYMENT_DUE),
    //   value = stringResource(Res.string.PAYMENTS_DUE_DESCRIPTION, chargingDay.format()),
    // )
    when (method) {
      is PayinAccount.SwishPayin -> {
        DetailRow(
          // TODO: Add "Number" / "Nummer" to Lokalise
          label = "Number",
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
        // The mandate holder comes from MemberPaymentInformation.chargeMethod.mandate, which nothing in this
        // feature queries yet.
        // DetailRow(
        //   label = stringResource(Res.string.PAYMENTS_MANDATE),
        //   value = mandate,
        // )
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
      // TODO: Add "Change number" / "Ändra nummer" to Lokalise
      is PayinAccount.SwishPayin -> "Change number"

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
      onClick = onRemoveMethod,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = { HedvigText(label) },
    endSlot = {
      HedvigText(
        text = value,
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth(),
        color = HedvigTheme.colorScheme.textSecondary,
      )
    },
    spaceBetween = 8.dp,
    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
  )
  HorizontalDivider(Modifier.padding(horizontal = 18.dp))
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
        navigateUp = {},
        onChangeMethod = {},
        onRemoveMethod = {},
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
