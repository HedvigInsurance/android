package com.hedvig.android.feature.home.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.Checkbox
import com.hedvig.android.design.system.hedvig.CheckboxOption
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults
import com.hedvig.android.design.system.hedvig.RadioGroupSize
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import hedvig.resources.CLAIMS_PLEDGE_SLIDE_LABEL
import hedvig.resources.HONESTY_PLEDGE_DESCRIPTION
import hedvig.resources.HONESTY_PLEDGE_HEADER
import hedvig.resources.HONESTY_PLEDGE_NOTE_1
import hedvig.resources.HONESTY_PLEDGE_NOTE_2
import hedvig.resources.HONESTY_PLEDGE_NOTE_3
import hedvig.resources.HONESTY_PLEDGE_TITLE
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StartClaimBottomSheet(
  state: HedvigBottomSheetState<Unit>,
  navigateToClaimChat: () -> Unit,
  navigateToClaimChatInDevMode: () -> Unit,
  isStagingEnvironment: Boolean,
) {
  HedvigBottomSheet(
    hedvigBottomSheetState = state,
    content = {
      StartClaimBottomSheetContent(
        state,
        navigateToClaimChat,
        navigateToClaimChatInDevMode,
        isStagingEnvironment,
      )
    },
  )
}

@Composable
private fun StartClaimBottomSheetContent(
  state: HedvigBottomSheetState<Unit>,
  navigateToClaimChat: () -> Unit,
  navigateToClaimChatInDevMode: () -> Unit,
  isStagingEnvironment: Boolean,
) {
  var isChecked by remember { mutableStateOf(false) }
  Column {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = stringResource(Res.string.HONESTY_PLEDGE_HEADER),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .semantics { heading() },
    )
    Spacer(Modifier.height(16.dp))
    PledgeNotes()
    Spacer(Modifier.height(16.dp))
    ImportantInfoCheckBox(
      isChecked = isChecked,
      onCheckedChange = {
        isChecked = !isChecked
      },
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      enabled = isChecked,
      onClick = dropUnlessResumed {
        state.dismiss {
          navigateToClaimChat()
        }
      },
      modifier = Modifier.fillMaxWidth(),
    )
    if (isStagingEnvironment) {
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = "Claim Chat (Dev)",
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        buttonSize = ButtonDefaults.ButtonSize.Small,
        onClick = dropUnlessResumed {
          state.dismiss {
            navigateToClaimChatInDevMode()
          }
        },
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_cancel_button),
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      onClick = {
        state.dismiss()
      },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun PledgeNotes() {
  Column {
    listOf(
      stringResource(Res.string.HONESTY_PLEDGE_NOTE_1),
      stringResource(Res.string.HONESTY_PLEDGE_NOTE_2),
      stringResource(Res.string.HONESTY_PLEDGE_NOTE_3),
    ).mapIndexed { index, text ->
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .horizontalDivider(DividerPosition.Top, index != 0)
          .padding(vertical = 16.dp),
      ) {
        Icon(HedvigIcons.Checkmark, null, Modifier.size(24.dp))
        HedvigText(text, Modifier.weight(1f), color = HedvigTheme.colorScheme.textSecondaryTranslucent)
      }
    }
  }
}

@Composable
private fun ImportantInfoCheckBox(isChecked: Boolean, onCheckedChange: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
      Column(modifier = Modifier.weight(1f)) {
        HedvigText(
          text = stringResource(Res.string.HONESTY_PLEDGE_TITLE),
          style = HedvigTheme.typography.label,
        )
        HedvigText(
          text = stringResource(Res.string.HONESTY_PLEDGE_DESCRIPTION),
          color = HedvigTheme.colorScheme.textSecondary,
          style = HedvigTheme.typography.label,
        )
        Spacer(Modifier.height(16.dp))
        HedvigTheme(darkTheme = false) {
          Checkbox(
            option = CheckboxOption(
              text = stringResource(Res.string.CLAIMS_PLEDGE_SLIDE_LABEL),
            ),
            selected = isChecked,
            onCheckboxSelected = onCheckedChange,
            size = RadioGroupSize.Small,
            colors = RadioGroupDefaults.colors.copy(containerColor = HedvigTheme.colorScheme.fillNegative),
          )
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewStartClaimBottomSheetContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      StartClaimBottomSheetContent(
        state = rememberHedvigBottomSheetState(),
        navigateToClaimChat = {},
        navigateToClaimChatInDevMode = {},
        isStagingEnvironment = true,
      )
    }
  }
}
