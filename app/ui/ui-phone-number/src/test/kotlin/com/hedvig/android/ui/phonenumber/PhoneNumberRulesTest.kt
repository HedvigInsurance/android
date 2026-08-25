package com.hedvig.android.ui.phonenumber

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.Test

internal class PhoneNumberRulesTest {
  private val member = PhoneNumberRules.MemberPhoneNumber
  private val swish = PhoneNumberRules.SwishPhoneNumber

  private fun PhoneNumberRules.edit(current: String, proposed: String): String =
    acceptEdit(current, proposed).toString()

  @Test
  fun `digits are accepted`() {
    assertThat(member.edit("070", "0701")).isEqualTo("0701")
  }

  @Test
  fun `a leading plus is accepted where it is allowed`() {
    assertThat(member.edit("", "+")).isEqualTo("+")
    assertThat(member.edit("+", "+46")).isEqualTo("+46")
  }

  @Test
  fun `a plus is refused where it is not allowed`() {
    assertThat(swish.edit("", "+")).isEqualTo("")
    assertThat(swish.edit("070", "+070")).isEqualTo("070")
  }

  @Test
  fun `a plus away from the start is refused`() {
    assertThat(member.edit("070", "07+0")).isEqualTo("070")
    assertThat(member.edit("+46", "++46")).isEqualTo("+46")
  }

  @Test
  fun `letters and separators are dropped from an edit`() {
    assertThat(member.edit("070", "070abc")).isEqualTo("070")
    assertThat(member.edit("070", "070-12")).isEqualTo("07012")
  }

  @Test
  fun `a line break is dropped from an edit`() {
    assertThat(member.edit("070", "070\n88")).isEqualTo("07088")
  }

  @Test
  fun `a number pasted with separators is cleaned rather than refused`() {
    assertThat(member.edit("", "070 123 45 67")).isEqualTo("0701234567")
    assertThat(member.edit("", "+46 70-123 45 67")).isEqualTo("+46701234567")
  }

  /**
   * Numbers get pasted with all sorts of stray whitespace around them. Dropping the + turns an
   * international number into a domestic one that does not exist, which is worse than refusing it.
   */
  @Test
  fun `a leading plus survives whitespace in front of it`() {
    assertThat(member.edit("", " +46701234567")).isEqualTo("+46701234567")
    assertThat(member.edit("", "\n+46 70 123 45 67")).isEqualTo("+46701234567")
  }

  @Test
  fun `a plus survives other junk in front of it`() {
    assertThat(member.edit("", "(+46) 70 123 45 67")).isEqualTo("+46701234567")
    assertThat(member.edit("", "Tel: +46 70 123 45 67")).isEqualTo("+46701234567")
  }

  /**
   * Stripping the plus would submit a domestic number that does not exist. Refusing the edit leaves
   * the member to retype it, which is recoverable in a way that a silently wrong number is not.
   */
  @Test
  fun `a plus that cannot be kept refuses the edit rather than being dropped`() {
    assertThat(member.edit("", "070 +46")).isEqualTo("")
    assertThat(swish.edit("", "+46701234567")).isEqualTo("")
  }

  @Test
  fun `non-ascii digits are refused so the rules agree with the backend`() {
    assertThat(member.edit("", "\u0660\u0661\u0662\u0663\u0664\u0665")).isEqualTo("")
    assertThat(member.hasEnoughDigits("\u0660\u0661\u0662\u0663\u0664\u0665")).isFalse()
  }

  /**
   * A value can hold more digits than the cap when the backend stored one, and every edit from there
   * still proposes something over the cap. Refusing those would leave no way back under it.
   */
  @Test
  fun `an over-long value can still be shortened`() {
    val seventeen = "12345678901234567"
    assertThat(member.edit(seventeen, seventeen.dropLast(1))).isEqualTo(seventeen.dropLast(1))
    assertThat(member.edit(seventeen, "")).isEqualTo("")
  }

  @Test
  fun `digits beyond the maximum are refused`() {
    val fifteen = "123456789012345"
    assertThat(member.edit(fifteen, fifteen + "6")).isEqualTo(fifteen)
    assertThat(member.edit("+$fifteen", "+${fifteen}6")).isEqualTo("+$fifteen")
  }

  @Test
  fun `a leading plus does not count towards the maximum`() {
    val fifteen = "123456789012345"
    assertThat(member.edit(fifteen, "+$fifteen")).isEqualTo("+$fifteen")
  }

  /**
   * The backend may hold a number that was never valid by these rules. It is shown so the member can
   * fix it, which means an edit must not quietly reshape the part they have not reached yet.
   */
  @Test
  fun `a stored malformed number survives an unrelated edit`() {
    assertThat(member.edit("070 123 45", "070 123 456")).isEqualTo("070 123 456")
  }

  @Test
  fun `a stored malformed number can be cleaned up by hand`() {
    assertThat(member.edit("070 123", "070123")).isEqualTo("070123")
  }

  @Test
  fun `an edit that makes a stored malformed number worse is refused`() {
    assertThat(member.edit("070 123", "070 123 a")).isEqualTo("070 123")
  }

  @Test
  fun `emptying the field is allowed`() {
    assertThat(member.edit("070", "")).isEqualTo("")
  }

  @Test
  fun `enough digits counts digits rather than characters`() {
    assertThat(member.hasEnoughDigits("+12345")).isFalse()
    assertThat(member.hasEnoughDigits("+123456")).isTrue()
    assertThat(member.hasEnoughDigits("070 123")).isTrue()
  }

  @Test
  fun `swish asks for more digits than the member number does`() {
    assertThat(swish.hasEnoughDigits("070123456")).isFalse()
    assertThat(swish.hasEnoughDigits("0701234567")).isTrue()
    assertThat(member.hasEnoughDigits("070123456")).isTrue()
  }
}
