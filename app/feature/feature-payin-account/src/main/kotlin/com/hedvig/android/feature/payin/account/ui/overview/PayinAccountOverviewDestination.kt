package com.hedvig.android.feature.payin.account.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ChevronRight
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.payin.account.data.InvoiceDelivery
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodRow
import com.hedvig.android.feature.payin.account.ui.components.PrimaryMethodLabel
import hedvig.resources.PAYMENT_ADD_METHOD_BUTTON
import hedvig.resources.PAYMENT_CHOOSE_PRIMARY_BUTTON
import hedvig.resources.PAYMENT_METHODS_TITLE
import hedvig.resources.Res
import octopus.type.MemberPaymentProvider
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PayinAccountOverviewDestination(
  viewModel: PayinAccountOverviewViewModel,
  onConnectPayoutMethodClicked: () -> Unit,
  onChoosePrimaryMethodClicked: () -> Unit,
  onPayinMethodClicked: (PayinAccount) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  PayinAccountOverviewScreen(
    uiState = uiState,
    onConnectPayoutMethodClicked = onConnectPayoutMethodClicked,
    onChoosePrimaryMethodClicked = onChoosePrimaryMethodClicked,
    onPayinMethodClicked = onPayinMethodClicked,
    onRetry = { viewModel.emit(PayinAccountOverviewEvent.Retry) },
    navigateUp = navigateUp,
  )
}

@Composable
private fun PayinAccountOverviewScreen(
  uiState: PayinAccountOverviewUiState,
  onConnectPayoutMethodClicked: () -> Unit,
  onChoosePrimaryMethodClicked: () -> Unit,
  onPayinMethodClicked: (PayinAccount) -> Unit,
  onRetry: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(Res.string.PAYMENT_METHODS_TITLE),
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    when (uiState) {
      PayinAccountOverviewUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced(
          Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      PayinAccountOverviewUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = onRetry,
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      is PayinAccountOverviewUiState.Content -> {
        PayoutAccountContent(
          currentMethods = uiState.currentMethods,
          availablePayinMethods = uiState.availablePayinMethods,
          onConnectPayinMethodClicked = onConnectPayoutMethodClicked,
          onChoosePrimaryMethodClicked = onChoosePrimaryMethodClicked,
          onPayinMethodClicked = onPayinMethodClicked,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun PayoutAccountContent(
  currentMethods: List<PayinAccount>,
  availablePayinMethods: List<MemberPaymentProvider>,
  onConnectPayinMethodClicked: () -> Unit,
  onChoosePrimaryMethodClicked: () -> Unit,
  onPayinMethodClicked: (PayinAccount) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier
      .verticalScroll(rememberScrollState()),
  ) {
    Spacer(Modifier.height(8.dp))
    if (currentMethods.isEmpty()) {
      if (availablePayinMethods.isNotEmpty()) {
        Spacer(Modifier.weight(1f))
        EmptyState(
          // TODO: Add "You haven't added a billing method yet. Add one to pay for your insurance." /
          //  "Du har inte lagt till någon betalningsmetod än. Lägg till en för att betala för din försäkring."
          //  to Lokalise
          text = "You haven’t added a billing method yet. Add one to pay for your insurance.",
          description = null,
          iconStyle = EmptyStateDefaults.EmptyStateIconStyle.INFO,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }
    } else {
      Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        for (method in currentMethods) {
          CurrentPayinMethodRow(method = method, onClick = { onPayinMethodClicked(method) })
        }
      }
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    if (availablePayinMethods.isNotEmpty()) {
      HedvigButton(
        text = stringResource(Res.string.PAYMENT_ADD_METHOD_BUTTON),
        onClick = onConnectPayinMethodClicked,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
    if (currentMethods.size > 1) {
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(Res.string.PAYMENT_CHOOSE_PRIMARY_BUTTON),
        onClick = onChoosePrimaryMethodClicked,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun CurrentPayinMethodRow(method: PayinAccount, onClick: () -> Unit, modifier: Modifier = Modifier) {
  HedvigCard(
    onClick = onClick,
    shape = HedvigTheme.shapes.cornerLarge,
    modifier = modifier.fillMaxWidth(),
  ) {
    PayinMethodRow(
      method = method,
      modifier = Modifier.fillMaxWidth(),
      endSlot = {
        if (method.isDefault) {
          PrimaryMethodLabel()
        }
        Icon(
          imageVector = HedvigIcons.ChevronRight,
          contentDescription = EmptyContentDescription,
          modifier = Modifier.size(24.dp),
        )
      },
    )
  }
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewPayinAccountOverviewScreen(
  @PreviewParameter(PayinAccountOverviewUiStateProvider::class) uiState: PayinAccountOverviewUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PayinAccountOverviewScreen(
        uiState = uiState,
        onConnectPayoutMethodClicked = {},
        onChoosePrimaryMethodClicked = {},
        onPayinMethodClicked = {},
        onRetry = {},
        navigateUp = {},
      )
    }
  }
}

private class PayinAccountOverviewUiStateProvider : CollectionPreviewParameterProvider<PayinAccountOverviewUiState>(
  listOf(
    PayinAccountOverviewUiState.Loading,
    PayinAccountOverviewUiState.Error,
    PayinAccountOverviewUiState.Content(
      currentMethods = emptyList(),
      availablePayinMethods = listOf(MemberPaymentProvider.SWISH, MemberPaymentProvider.TRUSTLY),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(
        PayinAccount.SwishPayin(
          phoneNumber = "0701234567",
          isPending = false,
          isDefault = true,
        ),
      ),
      availablePayinMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(
        PayinAccount.SwishPayin(
          phoneNumber = "0701234567",
          isPending = false,
          isDefault = true,
        ),
      ),
      availablePayinMethods = listOf(MemberPaymentProvider.SWISH, MemberPaymentProvider.TRUSTLY),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(PayinAccount.SwishPayin(phoneNumber = null, isPending = true, isDefault = true)),
      availablePayinMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(
        PayinAccount.Trustly(
          clearingNumber = "****",
          accountNumber = "*45678",
          bankName = "Swedbank",
          isPending = false,
          isDefault = true,
        ),
        PayinAccount.SwishPayin(
          phoneNumber = "0701234567",
          isPending = false,
          isDefault = false,
        ),
      ),
      availablePayinMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(
        PayinAccount.Trustly(
          clearingNumber = "****",
          accountNumber = "*45678",
          bankName = "Swedbank",
          isPending = false,
          isDefault = true,
        ),
        PayinAccount.SwishPayin(
          phoneNumber = "0701234567",
          isPending = false,
          isDefault = false,
        ),
        PayinAccount.Invoice(
          delivery = InvoiceDelivery.Kivra,
          isPending = false,
          isDefault = false,
          email = "",
        ),
      ),
      availablePayinMethods = listOf(MemberPaymentProvider.TRUSTLY),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(
        PayinAccount.Trustly(
          clearingNumber = "****",
          accountNumber = "*45678",
          bankName = "Swedbank",
          isPending = false,
          isDefault = true,
        ),
        PayinAccount.SwishPayin(
          phoneNumber = "0701234567",
          isPending = false,
          isDefault = false,
        ),
      ),
      availablePayinMethods = listOf(MemberPaymentProvider.TRUSTLY),
    ),
  ),
)
