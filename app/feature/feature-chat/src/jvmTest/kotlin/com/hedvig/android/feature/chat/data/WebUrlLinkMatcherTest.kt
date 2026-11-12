package com.hedvig.android.feature.chat.data

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.Test

class WebUrlLinkMatcherTest {
  @Test
  fun `a web URL matches whatever the case of its scheme`() {
    assertThat(webUrlLinkMatcher.matches("https://media.giphy.com/media/abc/giphy.gif")).isTrue()
    assertThat(webUrlLinkMatcher.matches("HTTPS://media.giphy.com/media/abc/giphy.gif")).isTrue()
    assertThat(webUrlLinkMatcher.matches("Http://media.giphy.com/media/abc/giphy.gif")).isTrue()
  }

  @Test
  fun `text that is not only a web URL does not match`() {
    assertThat(webUrlLinkMatcher.matches("look at https://media.giphy.com/media/abc/giphy.gif")).isFalse()
    assertThat(webUrlLinkMatcher.matches("ftp://example.com/a.gif")).isFalse()
  }
}
