package com.hedvig.android.shared.foreverui.ui.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.a11y.getDescription
import hedvig.resources.R

@Composable
internal fun ForeverExplanationBottomSheet(sheetState: HedvigBottomSheetState<UiMoney>) {
  HedvigBottomSheet(sheetState) { discount ->
    HedvigText(
      text = stringResource(id = R.string.referrals_info_sheet_headline),
      modifier = Modifier
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    val voiceOverDescription = stringResource(id = R.string.referrals_info_sheet_body, discount.getDescription())
    HedvigText(
      text = stringResource(id = R.string.referrals_info_sheet_body, discount.toString()),
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier
        .fillMaxWidth()
        .semantics {
          contentDescription = voiceOverDescription
        },
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.general_close_button),
      buttonSize = Large,
      onClick = { sheetState.dismiss() },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}
