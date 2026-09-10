package com.hedvig.android.feature.payin.account.ui.methoddetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Minus
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodMark
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodMarkSize
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodTile
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodTileBadge
import com.hedvig.android.feature.payin.account.ui.components.toRadioOption
import hedvig.resources.PAYMENT_REMOVE_SUBTITLE
import hedvig.resources.PAYMENT_REMOVE_TITLE
import hedvig.resources.REMOVE_CONFIRMATION_BUTTON
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RemovePayinMethodBottomSheet(
  sheetState: HedvigBottomSheetState<PayinAccount>,
  isRemoving: Boolean,
  onConfirmRemove: () -> Unit,
) {
  HedvigBottomSheet(sheetState) { method ->
    RemovePayinMethodBottomSheetContent(
      method = method,
      isRemoving = isRemoving,
      onConfirmRemove = onConfirmRemove,
      onDismiss = { sheetState.dismiss() },
    )
  }
}

@Composable
private fun RemovePayinMethodBottomSheetContent(
  method: PayinAccount,
  isRemoving: Boolean,
  onConfirmRemove: () -> Unit,
  onDismiss: () -> Unit,
) {
  HedvigText(
    text = stringResource(Res.string.PAYMENT_REMOVE_TITLE),
    textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth(),
  )
  HedvigText(
    text = stringResource(Res.string.PAYMENT_REMOVE_SUBTITLE),
    textAlign = TextAlign.Center,
    color = colorScheme.textSecondary,
    modifier = Modifier.fillMaxWidth(),
  )
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 64.dp),
  ) {
    PayinMethodTile(
      badge = {
        PayinMethodTileBadge(
          icon = HedvigIcons.Minus,
          containerColor = colorScheme.fillPrimary,
          contentColor = colorScheme.fillNegative,
        )
      },
      mark = { PayinMethodMark(method, Modifier.size(PayinMethodMarkSize)) },
    )
  }
  val option = method.toRadioOption()
  RadioGroup(
    options = listOf(option),
    selectedOption = option.id,
    onRadioOptionSelected = {},
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = stringResource(Res.string.REMOVE_CONFIRMATION_BUTTON),
    onClick = onConfirmRemove,
    enabled = !isRemoving,
    isLoading = isRemoving,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(8.dp))
  HedvigTextButton(
    text = stringResource(Res.string.general_cancel_button),
    onClick = onDismiss,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(16.dp))
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewRemovePayinMethodBottomSheetContent() {
  HedvigTheme {
    Surface(color = colorScheme.backgroundPrimary) {
      Column(
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      ) {
        RemovePayinMethodBottomSheetContent(
          method = PayinAccount.SwishPayin("0709901232", isPending = false, isDefault = false),
          isRemoving = false,
          onConfirmRemove = {},
          onDismiss = {},
        )
      }
    }
  }
}
