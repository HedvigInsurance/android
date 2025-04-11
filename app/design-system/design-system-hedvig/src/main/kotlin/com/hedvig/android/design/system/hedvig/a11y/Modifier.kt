package com.hedvig.android.design.system.hedvig.a11y

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import hedvig.resources.R

@Composable
fun Modifier.accessibilityForDropdown(labelText: String, selectedValue: String, isEnabled: Boolean): Modifier {
  val seeOptionsText = stringResource(R.string.TALKBACK_DOUBLE_TAP_TO_SEE_ALL_OPTIONS)
  return this then Modifier.clearAndSetSemantics {
    val description = "$labelText, $selectedValue, $seeOptionsText"
    contentDescription = description
    role = Role.DropdownList
    if (!isEnabled) {
      this.disabled()
    }
  }
}
