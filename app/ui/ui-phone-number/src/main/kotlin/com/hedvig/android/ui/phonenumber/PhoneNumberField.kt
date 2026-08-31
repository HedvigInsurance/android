package com.hedvig.android.ui.phonenumber

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.byValue
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.hedvig.android.core.common.validation.PhoneNumberRules
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

/**
 * A phone number field. Holds one line of digits under [rules], with the phone keypad and no return
 * key, so a number cannot arrive split across lines or carrying separators from whichever keyboard
 * the member happened to use.
 *
 * [rules] bound what can be typed, not whether what is there is long enough:
 * [PhoneNumberRules.minDigits] is for the caller to check with [PhoneNumberRules.hasEnoughDigits]
 * when it decides to submit, since each screen reports that differently.
 */
@Composable
fun HedvigPhoneNumberField(
  state: TextFieldState,
  labelText: String,
  rules: PhoneNumberRules,
  modifier: Modifier = Modifier,
  textFieldSize: HedvigTextFieldDefaults.TextFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
  errorState: HedvigTextFieldDefaults.ErrorState = HedvigTextFieldDefaults.ErrorState.NoError,
  enabled: Boolean = true,
  imeAction: ImeAction = ImeAction.Done,
  keyboardActions: KeyboardActionHandler? = null,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  HedvigTextField(
    state = state,
    labelText = labelText,
    textFieldSize = textFieldSize,
    errorState = errorState,
    enabled = enabled,
    inputTransformation = remember(rules) { phoneNumberInputTransformation(rules) },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = imeAction),
    keyboardActions = keyboardActions,
    lineLimits = TextFieldLineLimits.SingleLine,
    interactionSource = interactionSource,
    modifier = modifier,
  )
}

/**
 * Appends [digit] to a field governed by [rules], for a screen that feeds it from a keypad of its own.
 *
 * Necessary because [TextFieldState.edit] counts as a developer edit and so skips the field's
 * [InputTransformation], which runs only for input the member types. Appending directly would walk
 * straight past [PhoneNumberRules.maxDigits].
 */
fun TextFieldState.appendPhoneNumberDigit(digit: String, rules: PhoneNumberRules) {
  if (rules.digitsIn(text) + rules.digitsIn(digit) > rules.maxDigits) return
  edit {
    append(digit)
    placeCursorAtEnd()
  }
}

private fun phoneNumberInputTransformation(rules: PhoneNumberRules): InputTransformation =
  InputTransformation.byValue { current, proposed -> rules.acceptEdit(current, proposed) }

@HedvigPreview
@Composable
private fun PreviewHedvigPhoneNumberField() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HedvigPhoneNumberField(
        state = rememberTextFieldState("+46701234567"),
        labelText = "Phone number",
        rules = PhoneNumberRules.MemberPhoneNumber,
      )
    }
  }
}
