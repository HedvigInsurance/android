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
  fun digitsIn(text: CharSequence): Int = text.count { it.isAsciiDigit() }

  fun hasEnoughDigits(text: CharSequence): Boolean = digitsIn(text) >= minDigits

  /**
   * Whether [text] holds nothing but what these rules allow. The field keeps typed input this way on
   * its own, so the case worth checking before submitting is a stored value that was never valid.
   */
  fun isWellFormed(text: CharSequence): Boolean {
    val digits = withoutLeadingPlus(text)
    return digits.all { it.isAsciiDigit() } && digits.length <= maxDigits
  }

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
 * [rules] bound what can be typed, not whether what is there is long enough: [PhoneNumberRules.minDigits]
 * is for the caller to check with [PhoneNumberRules.hasEnoughDigits] when it decides to submit, since
 * each screen reports that differently.
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

  // Shortening an over-long value has to keep working, or a value already past the cap traps the
  // member: every edit, backspace included, proposes something still over it and would be refused.
  digitsIn(proposed) < digitsIn(current) && disallowedCount(proposed) <= disallowedCount(current) -> proposed

  isWellFormed(current) -> filterToAllowed(proposed)?.takeIf { withinMaxDigits(it) } ?: current

  disallowedCount(proposed) <= disallowedCount(current) && withinMaxDigits(proposed) -> proposed

  else -> current
}

/**
 * [text] reduced to digits, or null when it holds a `+` that cannot be kept.
 *
 * Dropping a `+` turns an international number into a domestic one that does not exist, so a `+` is
 * never removed on the member's behalf: it stays when it leads the number, and otherwise the whole
 * edit is refused. Separators carry no such meaning and are always stripped.
 */
private fun PhoneNumberRules.filterToAllowed(text: CharSequence): CharSequence? {
  if (!plusCanBeKept(text)) return null
  val digits = text.filter { it.isAsciiDigit() }
  return if (text.contains('+')) "+$digits" else digits
}

/** Whether any `+` in [text] is a single one leading the number, which these rules allow to stay. */
private fun PhoneNumberRules.plusCanBeKept(text: CharSequence): Boolean {
  val plusCount = text.count { it == '+' }
  if (plusCount == 0) return true
  if (!allowLeadingPlus || plusCount > 1) return false
  val firstDigit = text.indexOfFirst { it.isAsciiDigit() }
  return firstDigit == -1 || text.indexOf('+') < firstDigit
}

private fun PhoneNumberRules.withinMaxDigits(text: CharSequence): Boolean = digitsIn(text) <= maxDigits

private fun PhoneNumberRules.withoutLeadingPlus(text: CharSequence): CharSequence =
  if (allowLeadingPlus && text.firstOrNull() == '+') text.subSequence(1, text.length) else text

/** Characters these rules would have to strip. A leading `+` they allow is not one of them. */
private fun PhoneNumberRules.disallowedCount(text: CharSequence): Int =
  withoutLeadingPlus(text).count { !it.isAsciiDigit() }

/**
 * Deliberately not [Char.isDigit], which is true for Arabic-Indic and other non-ASCII digits that a
 * localised keyboard can produce and that the backend's own validation rejects.
 */
private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'

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
