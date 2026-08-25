package com.hedvig.android.ui.phonenumber

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.byValue
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

/**
 * What a phone number field accepts. Every count is in digits, so an allowed leading `+` never
 * counts towards [minDigits] or [maxDigits].
 *
 * A `+` is accepted but never interpreted: nothing here prepends a country code, drops a leading
 * zero, or reformats what the member typed. Deciding that `+46` and `07` mean the same thing needs a
 * country-code selector and a normalising step, and guessing at it produces numbers nobody can call.
 */
data class PhoneNumberRules(
  val minDigits: Int,
  val allowLeadingPlus: Boolean,
  val maxDigits: Int = E164_MAX_DIGITS,
) {
  fun digitsIn(text: CharSequence): Int = text.count { it.isDigit() }

  fun hasEnoughDigits(text: CharSequence): Boolean = digitsIn(text) >= minDigits

  /**
   * Whether [text] holds nothing but what these rules allow. The field keeps typed input this way on
   * its own, so the case worth checking before submitting is a stored value that was never valid.
   */
  fun isWellFormed(text: CharSequence): Boolean = disallowedCount(text) == 0 && withinMaxDigits(text)

  companion object {
    /** The most digits any international number holds, country code included. */
    const val E164_MAX_DIGITS = 15

    /**
     * The member's own number, which the backend keeps so we can reach them. Landlines run as short
     * as eight digits and foreign numbers are equally valid, so the minimum only has to rule out
     * input too short to be a number at all.
     */
    val MemberPhoneNumber = PhoneNumberRules(minDigits = 6, allowLeadingPlus = true)

    /**
     * Swish pays out to a Swedish mobile number, so the country code is never part of the input and
     * ten digits is the real length rather than a sanity floor.
     */
    val SwishPhoneNumber = PhoneNumberRules(minDigits = 10, allowLeadingPlus = false)
  }
}

/**
 * A phone number field. Holds one line of digits under [rules], with the phone keypad and no return
 * key, so a number cannot arrive split across lines or carrying separators from whichever keyboard
 * the member happened to use.
 *
 * [rules] bound what can be typed, not whether what is there is long enough: [minDigits] is for the
 * caller to check with [PhoneNumberRules.hasEnoughDigits] when it decides to submit, since each
 * screen reports that differently.
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
 * Keeps typed input to what [rules] allow.
 *
 * The stored number is shown exactly as the backend holds it, which is not necessarily well formed,
 * so that the member can correct it. That value is therefore never reshaped on their behalf: while
 * it is malformed, only edits that would add further disallowed characters are refused, so deleting
 * a stray separator or typing the rest of the number still works.
 *
 * Once the value is well formed, every disallowed character in an edit must be one this edit
 * introduced, so filtering the whole value is the same as filtering only what was inserted. That is
 * what lets a number pasted with spaces or dashes come out clean instead of being rejected whole.
 */
internal fun phoneNumberInputTransformation(rules: PhoneNumberRules): InputTransformation =
  InputTransformation.byValue { current, proposed -> rules.acceptEdit(current, proposed) }

/** The value an edit from [current] to [proposed] settles on. Pure, so the rules can be tested directly. */
internal fun PhoneNumberRules.acceptEdit(current: CharSequence, proposed: CharSequence): CharSequence = when {
  isWellFormed(proposed) -> proposed
  isWellFormed(current) -> filterToAllowed(proposed).takeIf { withinMaxDigits(it) } ?: current
  disallowedCount(proposed) <= disallowedCount(current) && withinMaxDigits(proposed) -> proposed
  else -> current
}

private fun PhoneNumberRules.withinMaxDigits(text: CharSequence): Boolean = digitsIn(text) <= maxDigits

private fun PhoneNumberRules.disallowedCount(text: CharSequence): Int =
  text.withIndex().count { (index, character) -> !isAllowed(character, index) }

/**
 * [text] reduced to what these rules allow. A `+` is judged by whether it leads the number rather
 * than by its index, so that whitespace pasted in front of one does not turn an international number
 * into a domestic number that nobody can call.
 */
private fun PhoneNumberRules.filterToAllowed(text: CharSequence): CharSequence {
  val digits = text.filter { it.isDigit() }
  val leadsWithPlus = allowLeadingPlus && text.firstOrNull { !it.isWhitespace() } == '+'
  return if (leadsWithPlus) "+$digits" else digits
}

private fun PhoneNumberRules.isAllowed(character: Char, index: Int): Boolean =
  character.isDigit() || (allowLeadingPlus && character == '+' && index == 0)

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
