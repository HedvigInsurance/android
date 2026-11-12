package com.hedvig.android.shared.foreverui.ui.ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.eygraber.uri.Uri
import org.junit.Test

/**
 * [ForeverViewModel.referralShareUrl] percent-encodes the referral code with [Uri.encode]. The expected values are what
 * `android.net.Uri.encode` produces: letters, digits and `_-!.~'()*` stay as they are, everything else becomes UTF-8
 * percent-encoded, and a space becomes `%20`.
 */
class ReferralCodeUrlEncodingTest {
  @Test
  fun `letters and digits are unchanged`() {
    assertThat(Uri.encode("HEDV1G")).isEqualTo("HEDV1G")
    assertThat(Uri.encode("abc123")).isEqualTo("abc123")
  }

  @Test
  fun `unreserved punctuation is unchanged`() {
    assertThat(Uri.encode("_-!.~'()*")).isEqualTo("_-!.~'()*")
  }

  @Test
  fun `space becomes percent 20`() {
    assertThat(Uri.encode("MY CODE")).isEqualTo("MY%20CODE")
  }

  @Test
  fun `reserved characters are percent-encoded`() {
    assertThat(Uri.encode("A+B")).isEqualTo("A%2BB")
    assertThat(Uri.encode("A/B")).isEqualTo("A%2FB")
    assertThat(Uri.encode("A&B")).isEqualTo("A%26B")
    assertThat(Uri.encode("A%B")).isEqualTo("A%25B")
  }

  @Test
  fun `non-ASCII characters are percent-encoded as UTF-8`() {
    assertThat(Uri.encode("KÖD")).isEqualTo("K%C3%96D")
    assertThat(Uri.encode("å")).isEqualTo("%C3%A5")
  }
}
