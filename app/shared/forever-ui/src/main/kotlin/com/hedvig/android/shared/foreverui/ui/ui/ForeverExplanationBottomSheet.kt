package com.hedvig.android.shared.foreverui.ui.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import hedvig.resources.R

@Composable
internal fun ForeverExplanationBottomSheet(isVisible: Boolean, discount: String, onDismiss: () -> Unit) {
  HedvigBottomSheet(
    isVisible = isVisible,
    onVisibleChange = { onDismiss() },
  ) {
    HedvigText(
      text = stringResource(id = R.string.referrals_info_sheet_headline),
      modifier = Modifier
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = stringResource(id = R.string.referrals_info_sheet_body, discount),
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.general_close_button),
      onClick = { onDismiss() },
      modifier = Modifier
        .padding(bottom = 16.dp)
        .fillMaxWidth(),
    )
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}
