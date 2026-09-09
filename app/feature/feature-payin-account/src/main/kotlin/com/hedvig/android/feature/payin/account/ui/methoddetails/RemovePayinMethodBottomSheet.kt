package com.hedvig.android.feature.payin.account.ui.methoddetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.dashedBorder
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Minus
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodMark
import com.hedvig.android.feature.payin.account.ui.components.toRadioOption
import hedvig.resources.PAYMENT_REMOVE_SUBTITLE
import hedvig.resources.PAYMENT_REMOVE_TITLE
import hedvig.resources.REMOVE_CONFIRMATION_BUTTON
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import org.jetbrains.compose.resources.stringResource

private val IllustrationTileSize = 74.dp
private val IllustrationMarkSize = 39.dp
private val RemoveBadgeSize = 24.dp

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
    RemovedMethodIllustration(method)
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

/** The method's tile drawn as an outline with a minus badge: what is about to be given up. */
@Composable
private fun RemovedMethodIllustration(method: PayinAccount, modifier: Modifier = Modifier) {
  Box(modifier) {
    Surface(
      shape = HedvigTheme.shapes.cornerXXLarge,
      color = colorScheme.backgroundPrimary,
      contentColor = colorScheme.fillPrimary,
      modifier = Modifier
        .size(IllustrationTileSize)
        .hedvigDropShadow(HedvigTheme.shapes.cornerXXLarge)
        .dashedBorder(
          color = colorScheme.borderSecondary,
          shape = HedvigTheme.shapes.cornerXXLarge,
        ),
    ) {
      Box(Modifier.size(IllustrationTileSize), contentAlignment = Alignment.Center) {
        PayinMethodMark(method, Modifier.size(IllustrationMarkSize))
      }
    }
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .offset(x = 8.dp, y = (-8).dp)
        .size(RemoveBadgeSize)
        .clip(CircleShape)
        .background(colorScheme.fillPrimary),
    ) {
      Icon(
        imageVector = HedvigIcons.Minus,
        contentDescription = EmptyContentDescription,
        tint = colorScheme.fillNegative,
        modifier = Modifier.size(16.dp),
      )
    }
  }
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
