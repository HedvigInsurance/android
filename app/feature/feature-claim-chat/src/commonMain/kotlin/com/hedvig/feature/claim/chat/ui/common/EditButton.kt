package com.hedvig.feature.claim.chat.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.icon.ChevronDown
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import hedvig.resources.Res
import hedvig.resources.claims_edit_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EditButton(canBeChanged: Boolean, onRegret: () -> Unit, modifier: Modifier = Modifier) {
  if (canBeChanged) {
    RoundCornersPill(
      onClick = onRegret,
      modifier = modifier
        .fillMaxWidth()
        .wrapContentWidth(Alignment.End)
        .semantics(true) {
          role = Role.Button
        },
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        HedvigText(
          text = stringResource(Res.string.claims_edit_button),
          fontStyle = HedvigTheme.typography.label.fontStyle,
        )
        Icon(
          imageVector = HedvigIcons.ChevronDown,
          contentDescription = null,
          tint = HedvigTheme.colorScheme.fillTertiaryTransparent,
        )
      }
    }
  }
}
