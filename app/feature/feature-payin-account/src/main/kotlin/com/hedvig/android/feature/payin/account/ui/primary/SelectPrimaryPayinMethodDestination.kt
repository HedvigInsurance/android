package com.hedvig.android.feature.payin.account.ui.primary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconResource
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadFilled
import com.hedvig.android.design.system.hedvig.icon.Trustly
import com.hedvig.android.design.system.hedvig.icon.colored.Kivra
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.data.provider
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodRow
import com.hedvig.android.feature.payin.account.ui.components.PrimaryMethodLabel
import com.hedvig.android.feature.payin.account.ui.components.payinMethodSubtitle
import com.hedvig.android.feature.payin.account.ui.components.payinMethodTitle
import hedvig.resources.PAYMENT_PRIMARY_CONFIRM_TITLE
import hedvig.resources.PAYMENT_PRIMARY_SUBTITLE
import hedvig.resources.PAYMENT_PRIMARY_TITLE
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SelectPrimaryPayinMethodDestination(
  viewModel: SelectPrimaryPayinMethodViewModel,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState.hasSetPrimaryMethod) {
    if (uiState.hasSetPrimaryMethod) {
      navigateBack()
    }
  }
  SelectPrimaryPayinMethodScreen(
    uiState = uiState,
    onMethodSelected = { viewModel.emit(SelectPrimaryPayinMethodEvent.SelectMethod(it)) },
    onConfirm = { viewModel.emit(SelectPrimaryPayinMethodEvent.ConfirmSelectedMethod) },
    navigateUp = navigateUp,
    navigateBack = navigateBack,
  )
}

@Composable
private fun SelectPrimaryPayinMethodScreen(
  uiState: SelectPrimaryPayinMethodUiState,
  onMethodSelected: (PayinAccount) -> Unit,
  onConfirm: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  val confirmationSheetState = rememberHedvigBottomSheetState<PayinAccount>()
  HedvigScaffold(
    topAppBarText = null,
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.height(8.dp))
    Column(
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HedvigText(
        text = stringResource(Res.string.PAYMENT_PRIMARY_TITLE),
        textAlign = TextAlign.Center,
      )
      HedvigText(
        text = stringResource(Res.string.PAYMENT_PRIMARY_SUBTITLE),
        color = colorScheme.textSecondary,
        textAlign = TextAlign.Center,
      )
    }
    Spacer(Modifier.height(48.dp))
    PayinMethodHandoverIllustration(
      method = uiState.selectedMethod,
      modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(48.dp))
    RadioGroup(
      options = uiState.methods.map { it.toRadioOption() },
      selectedOption = uiState.selectedMethod?.let { RadioOptionId(it.provider.rawValue) },
      onRadioOptionSelected = { id ->
        val method = uiState.methods.firstOrNull { it.provider.rawValue == id.id }
        if (method != null) onMethodSelected(method)
      },
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    if (uiState.errorMessage != null) {
      Spacer(Modifier.height(8.dp))
      HedvigNotificationCard(
        message = uiState.errorMessage,
        priority = NotificationPriority.Error,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      onClick = {
        val method = uiState.selectedMethod
        if (method != null) confirmationSheetState.show(method)
      },
      enabled = uiState.selectedMethod != null,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_cancel_button),
      onClick = navigateBack,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
  ConfirmPrimaryPayinMethodBottomSheet(
    sheetState = confirmationSheetState,
    isConfirming = uiState.isConfirming,
    onConfirm = onConfirm,
  )
}

@Composable
private fun ConfirmPrimaryPayinMethodBottomSheet(
  sheetState: HedvigBottomSheetState<PayinAccount>,
  isConfirming: Boolean,
  onConfirm: () -> Unit,
) {
  HedvigBottomSheet(sheetState) { method ->
    HedvigText(
      text = stringResource(Res.string.PAYMENT_PRIMARY_CONFIRM_TITLE),
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    HedvigNotificationCard(
      // TODO: Add "Your next payment will be drawn from {method}. Claims payouts will also be sent to this
      //  account." / "Din nästa betalning dras från {method}. Skadeutbetalningar skickas också till detta konto."
      //  to Lokalise
      message = "Your next payment will be drawn from ${payinMethodTitle(method)}. " +
        "Claims payouts will also be sent to this account.",
      priority = NotificationPriority.Info,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Surface(
      shape = HedvigTheme.shapes.cornerLarge,
      color = colorScheme.surfacePrimary,
      modifier = Modifier.fillMaxWidth(),
    ) {
      PayinMethodRow(
        method = method,
        modifier = Modifier.fillMaxWidth(),
        endSlot = { PrimaryMethodLabel() },
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      onClick = onConfirm,
      enabled = !isConfirming,
      isLoading = isConfirming,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_cancel_button),
      onClick = { sheetState.dismiss() },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

/** The chosen method handing over to Hedvig: the method's mark, a run of dots, then the Hedvig mark. */
@Composable
private fun PayinMethodHandoverIllustration(method: PayinAccount?, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Surface(
      shape = HedvigTheme.shapes.cornerXXLarge,
      color = colorScheme.fillWhite,
      contentColor = colorScheme.fillBlack,
      border = colorScheme.borderSecondary,
      modifier = Modifier.size(74.dp),
    ) {
      Box(Modifier.size(74.dp), contentAlignment = Alignment.Center) {
        when (method) {
          is PayinAccount.Trustly -> {
            Icon(HedvigIcons.Trustly, EmptyContentDescription, Modifier.size(39.dp))
          }

          is PayinAccount.SwishPayin -> {
            Icon(HedvigIcons.Swish, EmptyContentDescription, Modifier.size(39.dp))
          }

          is PayinAccount.Invoice -> {
            Icon(HedvigIcons.Kivra, EmptyContentDescription, Modifier.size(39.dp))
          }

          null -> {}
        }
      }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      repeat(3) { index ->
        Surface(
          shape = HedvigTheme.shapes.cornerFull,
          color = if (index == 0) colorScheme.fillPrimary else colorScheme.borderSecondary,
          modifier = Modifier.size(6.dp),
        ) {}
      }
    }
    Surface(
      shape = HedvigTheme.shapes.cornerXXLarge,
      color = colorScheme.fillBlack,
      contentColor = colorScheme.fillWhite,
      modifier = Modifier.size(74.dp),
    ) {
      Box(Modifier.size(74.dp), contentAlignment = Alignment.Center) {
        Icon(HedvigIcons.HelipadFilled, EmptyContentDescription, Modifier.size(39.dp))
      }
    }
  }
}

@Composable
private fun PayinAccount.toRadioOption(): RadioOption = RadioOption(
  id = RadioOptionId(provider.rawValue),
  text = payinMethodTitle(this),
  label = payinMethodSubtitle(this),
  iconResource = when (this) {
    is PayinAccount.Trustly -> IconResource.Vector(HedvigIcons.Trustly)
    is PayinAccount.SwishPayin -> IconResource.Vector(HedvigIcons.Swish)
    is PayinAccount.Invoice -> IconResource.Vector(HedvigIcons.Kivra)
  },
)

@Composable
@HedvigPreview
private fun PreviewSelectPrimaryPayinMethodScreen() {
  HedvigTheme {
    Surface(color = colorScheme.backgroundPrimary) {
      val methods = listOf(
        PayinAccount.Trustly("8327", "91234124", "Swedbank", isPending = false, isDefault = true),
        PayinAccount.SwishPayin("0709901232", isPending = false, isDefault = false),
      )
      SelectPrimaryPayinMethodScreen(
        uiState = SelectPrimaryPayinMethodUiState(methods = methods, selectedMethod = methods[1]),
        onMethodSelected = {},
        onConfirm = {},
        navigateUp = {},
        navigateBack = {},
      )
    }
  }
}
