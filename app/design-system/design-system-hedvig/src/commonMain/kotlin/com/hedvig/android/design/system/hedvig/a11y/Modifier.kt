package com.hedvig.android.design.system.hedvig.a11y

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import hedvig.resources.Res
import hedvig.resources.TALKBACK_DOUBLE_TAP_TO_CHOOSE
import hedvig.resources.TALKBACK_DOUBLE_TAP_TO_SEE_ALL_OPTIONS
import org.jetbrains.compose.resources.stringResource

@Composable
fun Modifier.accessibilityForDropdown(labelText: String, selectedValue: String, isEnabled: Boolean): Modifier {
  val seeOptionsText = stringResource(Res.string.TALKBACK_DOUBLE_TAP_TO_SEE_ALL_OPTIONS)
  return this then Modifier.clearAndSetSemantics {
    val description = "$labelText, $selectedValue, $seeOptionsText"
    contentDescription = description
    role = Role.DropdownList
    if (!isEnabled) {
      this.disabled()
    }
  }
}

/**
 * Makes a field that opens a date picker read as a single button: "[labelText], [selectedDate], Button,
 * Double tap to choose". Replaces the semantics of everything inside it, so a read-only text field drawn underneath
 * is not announced as an edit box.
 *
 * [onClick] is what a TalkBack double tap runs, so pass the same handler that finger taps use.
 */
@Composable
fun Modifier.accessibilityForDatePickerField(
  labelText: String,
  selectedDate: String?,
  isEnabled: Boolean,
  onClick: () -> Unit,
  errorText: String? = null,
): Modifier {
  val chooseText = stringResource(Res.string.TALKBACK_DOUBLE_TAP_TO_CHOOSE)
  return this then Modifier.clearAndSetSemantics {
    contentDescription = labelText
    selectedDate?.let { stateDescription = it }
    role = Role.Button
    errorText?.let { error(it) }
    if (isEnabled) {
      onClick(label = chooseText) {
        onClick()
        true
      }
    } else {
      disabled()
    }
  }
}
