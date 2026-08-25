package com.hedvig.android.core.common.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneNumberRulesTest {
  private val member = PhoneNumberRules.MemberPhoneNumber
  private val swish = PhoneNumberRules.SwishPhoneNumber

  private fun PhoneNumberRules.edit(current: String, proposed: String): String =
    acceptEdit(current, proposed).toString()

  @Test
  fun `digits are accepted`() {
    assertEquals("0701", member.edit("070", "0701"))
  }

  @Test
  fun `a leading plus is accepted where it is allowed`() {
    assertEquals("+", member.edit("", "+"))
    assertEquals("+46", member.edit("+", "+46"))
  }

  @Test
  fun `a plus is refused where it is not allowed`() {
    assertEquals("", swish.edit("", "+"))
    assertEquals("070", swish.edit("070", "+070"))
  }

  @Test
  fun `a plus away from the start is refused`() {
    assertEquals("070", member.edit("070", "07+0"))
    assertEquals("+46", member.edit("+46", "++46"))
  }

  @Test
  fun `letters and separators are dropped from an edit`() {
    assertEquals("070", member.edit("070", "070abc"))
    assertEquals("07012", member.edit("070", "070-12"))
  }

  @Test
  fun `a line break is dropped from an edit`() {
    assertEquals("07088", member.edit("070", "070\n88"))
  }

  @Test
  fun `a number pasted with separators is cleaned rather than refused`() {
    assertEquals("0701234567", member.edit("", "070 123 45 67"))
    assertEquals("+46701234567", member.edit("", "+46 70-123 45 67"))
  }

  /**
   * Numbers get pasted with all sorts of junk around them. Dropping the + turns an international
   * number into a domestic one that does not exist, which is worse than refusing the edit.
   */
  @Test
  fun `a plus survives whitespace and other junk in front of it`() {
    assertEquals("+46701234567", member.edit("", " +46701234567"))
    assertEquals("+46701234567", member.edit("", "\n+46 70 123 45 67"))
    assertEquals("+46701234567", member.edit("", "(+46) 70 123 45 67"))
    assertEquals("+46701234567", member.edit("", "Tel: +46 70 123 45 67"))
  }

  @Test
  fun `a plus that cannot be kept refuses the edit rather than being dropped`() {
    assertEquals("", member.edit("", "070 +46"))
    assertEquals("", swish.edit("", "+46701234567"))
  }

  @Test
  fun `non-ascii digits are refused so the rules agree with the backend`() {
    val arabicIndic = "٠١٢٣٤٥"
    assertEquals("", member.edit("", arabicIndic))
    assertFalse(member.hasEnoughDigits(arabicIndic))
    assertFalse(member.isWellFormed(arabicIndic))
  }

  @Test
  fun `digits beyond the maximum are refused`() {
    val fifteen = "123456789012345"
    assertEquals(fifteen, member.edit(fifteen, fifteen + "6"))
    assertEquals("+$fifteen", member.edit("+$fifteen", "+${fifteen}6"))
  }

  @Test
  fun `a leading plus does not count towards the maximum`() {
    val fifteen = "123456789012345"
    assertEquals("+$fifteen", member.edit(fifteen, "+$fifteen"))
  }

  /**
   * A value can hold more digits than the cap when it was stored that way, and every edit from there
   * still proposes something over the cap. Refusing those would leave no way back under it.
   */
  @Test
  fun `an over-long value can still be shortened`() {
    val seventeen = "12345678901234567"
    assertEquals(seventeen.dropLast(1), member.edit(seventeen, seventeen.dropLast(1)))
    assertEquals("", member.edit(seventeen, ""))
  }

  /**
   * A stored number may never have been valid by these rules. It is shown so the member can fix it,
   * which means an edit must not quietly reshape the part they have not reached yet.
   */
  @Test
  fun `a stored malformed number survives an unrelated edit`() {
    assertEquals("070 123 456", member.edit("070 123 45", "070 123 456"))
  }

  @Test
  fun `a stored malformed number can be cleaned up by hand`() {
    assertEquals("070123", member.edit("070 123", "070123"))
  }

  @Test
  fun `an edit that makes a stored malformed number worse is refused`() {
    assertEquals("070 123", member.edit("070 123", "070 123 a"))
  }

  @Test
  fun `emptying the field is allowed`() {
    assertEquals("", member.edit("070", ""))
  }

  @Test
  fun `enough digits counts digits rather than characters`() {
    assertFalse(member.hasEnoughDigits("+12345"))
    assertTrue(member.hasEnoughDigits("+123456"))
    assertTrue(member.hasEnoughDigits("070 123"))
  }

  @Test
  fun `swish asks for more digits than the member number does`() {
    assertFalse(swish.hasEnoughDigits("070123456"))
    assertTrue(swish.hasEnoughDigits("0701234567"))
    assertTrue(member.hasEnoughDigits("070123456"))
  }

  /**
   * The shapes the profile screen's own tests pin down, kept here so the shared rules cannot drift
   * away from what that screen has always accepted.
   */
  @Test
  fun `the shapes profile accepts stay accepted`() {
    for (valid in listOf("+1234567890123", "1234567890123", "1234567890123")) {
      assertTrue(member.isWellFormed(valid), valid)
      assertTrue(member.hasEnoughDigits(valid), valid)
    }
  }

  @Test
  fun `the shapes profile rejects stay rejected`() {
    for (malformed in listOf("++1234", "+1234a")) {
      assertFalse(member.isWellFormed(malformed), malformed)
    }
    for (tooShort in listOf("+", "+1", "1", "+12345", "12345")) {
      assertFalse(member.hasEnoughDigits(tooShort), tooShort)
    }
  }
}
