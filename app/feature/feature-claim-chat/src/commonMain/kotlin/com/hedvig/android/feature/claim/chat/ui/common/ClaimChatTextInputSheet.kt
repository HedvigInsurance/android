package com.hedvig.android.feature.claim.chat.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import hedvig.resources.CLAIMS_TEXT_INPUT_POPOVER_PLACEHOLDER
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.general_save_button
import org.jetbrains.compose.resources.stringResource

/**
 * Text input for a claim chat step, presented over the conversation so the question stays readable above it.
 *
 * The sheet is either open or closed and carries nothing of its own, so its state is typed [Unit] like the rest of
 * the app's sheets. The draft starts from [initialText] each time it opens, and cancelling leaves the saved value
 * alone.
 */
@Composable
internal fun ClaimChatTextInputSheet(
  sheetState: HedvigBottomSheetState<Unit>,
  initialText: String,
  maxLength: Int,
  onSave: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigBottomSheet(
    hedvigBottomSheetState = sheetState,
    modifier = modifier,
    contentPadding = ClaimChatInputSheet.padding,
    style = ClaimChatInputSheet.style,
  ) {
    // Keyed on visibility so each opening starts from whatever is currently saved rather than the last draft.
    var text by remember(sheetState.isVisible) { mutableStateOf(initialText) }

    HedvigTextField(
      text = text,
      onValueChange = { if (it.length <= maxLength) text = it },
      labelText = stringResource(Res.string.CLAIMS_TEXT_INPUT_POPOVER_PLACEHOLDER),
      textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Large,
      singleLine = false,
      minLines = 3,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      HedvigButton(
        text = stringResource(Res.string.general_cancel_button),
        onClick = { sheetState.dismiss() },
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
        modifier = Modifier.weight(1f),
      )
      HedvigButton(
        text = stringResource(Res.string.general_save_button),
        onClick = {
          onSave(text)
          sheetState.dismiss()
        },
        enabled = text.isNotBlank(),
        modifier = Modifier.weight(1f),
      )
    }
    Spacer(Modifier.height(8.dp))
  }
}
