package com.hedvig.core.common.android.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsValidEmailTest {
  @Test
  fun acceptsValidEmails() {
    listOf(
      "a@b.co",
      "first.last@example.com",
      "user+tag@sub.example.org",
      "USER_1%x-y@example-domain.se",
    ).forEach { email ->
      assertTrue(isValidEmail(email), "Expected \"$email\" to be valid")
    }
  }

  @Test
  fun rejectsInvalidEmails() {
    listOf(
      "",
      "   ",
      "plainaddress",
      "@example.com",
      "user@",
      "user@example",
      "user name@example.com",
      " user@example.com",
      "user@example.com ",
      "user@@example.com",
    ).forEach { email ->
      assertFalse(isValidEmail(email), "Expected \"$email\" to be invalid")
    }
  }
}
