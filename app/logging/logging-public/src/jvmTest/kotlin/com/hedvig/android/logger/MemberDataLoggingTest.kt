package com.hedvig.android.logger

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.After
import org.junit.Test

class MemberDataLoggingTest {
  @After
  fun tearDown() {
    MemberDataLogging.isAllowed = false
  }

  @Test
  fun `member data is redacted until a host allows it`() {
    assertThat(memberData("191212121212")).isEqualTo("<redacted>")
  }

  @Test
  fun `member data is printed in full where it is allowed`() {
    MemberDataLogging.isAllowed = true

    assertThat(memberData("191212121212")).isEqualTo("191212121212")
    assertThat(memberData(null)).isEqualTo("null")
  }

  @Test
  fun `the replacement is used when member data is redacted`() {
    assertThat(memberData(listOf("a", "b"), replacement = "2 items")).isEqualTo("2 items")
  }

  @Test
  fun `a redacted url keeps its path and loses its query and fragment`() {
    val signedUrl = "https://bucket.s3.amazonaws.com/folder/file.pdf?X-Amz-Signature=abc&X-Amz-Expires=600#page=2"

    assertThat(urlForLog(signedUrl)).isEqualTo("https://bucket.s3.amazonaws.com/folder/file.pdf")
  }

  @Test
  fun `a url is printed in full where member data is allowed`() {
    MemberDataLogging.isAllowed = true
    val signedUrl = "https://bucket.s3.amazonaws.com/folder/file.pdf?X-Amz-Signature=abc"

    assertThat(urlForLog(signedUrl)).isEqualTo(signedUrl)
  }

  @Test
  fun `a redacted url without a query is unchanged and null stays null`() {
    assertThat(urlForLog("hedvig://chat")).isEqualTo("hedvig://chat")
    assertThat(urlForLog(null)).isEqualTo("null")
  }

  @Test
  fun `url queries are removed from inside a longer text and the rest is kept`() {
    val key = "ShowCertificateKey(url=Url(uri=https://bucket.test/a/b.pdf?X-Amz-Signature=abc&X-Amz-Expires=60), id=42)"

    assertThat(key.withoutUrlQueries()).isEqualTo("ShowCertificateKey(url=Url(uri=https://bucket.test/a/b.pdf), id=42)")
  }

  @Test
  fun `text without a url is unchanged`() {
    assertThat("ContractDetailKey(contractId=abc?def)".withoutUrlQueries())
      .isEqualTo("ContractDetailKey(contractId=abc?def)")
  }

  @Test
  fun `a custom scheme url loses its query`() {
    assertThat(urlForLog("bankid:///?autostarttoken=abc&redirect=null")).isEqualTo("bankid:///")
  }
}
