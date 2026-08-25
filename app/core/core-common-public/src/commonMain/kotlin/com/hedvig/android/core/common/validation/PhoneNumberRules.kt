package com.hedvig.android.core.common.validation

/**
 * What counts as a phone number, for the field the member types it into and for whatever validates it
 * before it is sent. Every count is in digits, so an allowed leading `+` never counts towards
 * [minDigits] or [maxDigits].
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
   * Whether [text] holds nothing but what these rules allow. A field built on these rules keeps typed
   * input this way on its own, so the case worth checking before submitting is a stored value that
   * was never valid.
   */
  fun isWellFormed(text: CharSequence): Boolean {
    val digits = withoutLeadingPlus(text)
    return digits.all { it.isAsciiDigit() } && digits.length <= maxDigits
  }

  /**
   * The value an edit from [current] to [proposed] settles on, for a field that accepts these rules.
   *
   * A stored number is shown exactly as it is held, which is not necessarily well formed, so that the
   * member can correct it. That value is never reshaped on their behalf: while it is malformed, only
   * edits that would add further disallowed characters are refused, so deleting a stray separator or
   * typing the rest of the number still works.
   *
   * Once the value is well formed, every disallowed character in an edit must be one that edit
   * introduced, so filtering the whole value is the same as filtering only what was inserted. That is
   * what lets a number pasted with spaces or dashes come out clean instead of being rejected whole.
   */
  fun acceptEdit(current: CharSequence, proposed: CharSequence): CharSequence = when {
    isWellFormed(proposed) -> proposed

    // Shortening an over-long value has to keep working, or a value already past the cap traps the
    // member: every edit, backspace included, proposes something still over it and would be refused.
    digitsIn(proposed) < digitsIn(current) && disallowedCount(proposed) <= disallowedCount(current) -> proposed

    isWellFormed(current) -> filterToAllowed(proposed)?.takeIf { digitsIn(it) <= maxDigits } ?: current

    disallowedCount(proposed) <= disallowedCount(current) && digitsIn(proposed) <= maxDigits -> proposed

    else -> current
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
