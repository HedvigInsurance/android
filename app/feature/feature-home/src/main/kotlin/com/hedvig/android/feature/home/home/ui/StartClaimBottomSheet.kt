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
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.Checkbox
import com.hedvig.android.design.system.hedvig.CheckboxOption
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults
import com.hedvig.android.design.system.hedvig.RadioGroupSize
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import hedvig.resources.CLAIMS_PLEDGE_SLIDE_LABEL
import hedvig.resources.HONESTY_PLEDGE_DESCRIPTION
import hedvig.resources.HONESTY_PLEDGE_TITLE
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StartClaimBottomSheet(
  state: HedvigBottomSheetState<Unit>,
  navigateToOldClaimFlow: () -> Unit,
  navigateToClaimChat: () -> Unit,
  navigateToClaimChatInDevMode: () -> Unit,
  isExperimentalClaimChatEnabled: Boolean,
  isStagingEnvironment: Boolean,
) {
  HedvigBottomSheet(
    hedvigBottomSheetState = state,
    content = {
      var isChecked by remember { mutableStateOf(false) }
      Column {
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
              if (isExperimentalClaimChatEnabled) {
                navigateToClaimChat()
              } else {
                navigateToOldClaimFlow()
              }
            }
          },
          modifier = Modifier.fillMaxWidth(),
        )
        if (isStagingEnvironment) {
          Spacer(Modifier.height(16.dp))
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            HedvigButton(
              text = if (isExperimentalClaimChatEnabled) {
                "Old claim flow"
              } else {
                "New claim chat"
              },
              enabled = true,
              buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
              buttonSize = ButtonDefaults.ButtonSize.Small,
              onClick = dropUnlessResumed {
                state.dismiss {
                  if (isExperimentalClaimChatEnabled) {
                    navigateToOldClaimFlow()
                  } else {
                    navigateToClaimChat()
                  }
                }
              },
              modifier = Modifier.weight(1f),
            )
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
              modifier = Modifier.weight(1f),
            )
          }
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
    },
  )
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
          style = HedvigTheme.typography.headlineSmall,
        )
        HedvigText(
          text = stringResource(Res.string.HONESTY_PLEDGE_DESCRIPTION),
          color = HedvigTheme.colorScheme.textSecondary,
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
