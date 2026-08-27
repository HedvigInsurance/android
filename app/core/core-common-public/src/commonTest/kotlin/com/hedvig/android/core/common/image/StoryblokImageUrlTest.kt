package com.hedvig.android.core.common.image

import kotlin.test.Test
import kotlin.test.assertEquals

class StoryblokImageUrlTest {
  private val pillow = "https://a.storyblok.com/f/165473/832x832/8a49d50630/cat-pillow-832x832px.png"

  @Test
  fun `appends the resize transform to a storyblok asset url`() {
    assertEquals("$pillow/m/126x126", storyblokResized(pillow, 126))
  }

  @Test
  fun `keeps a query string after the transform`() {
    assertEquals("$pillow/m/126x126?cv=1234", storyblokResized("$pillow?cv=1234", 126))
  }

  @Test
  fun `does not double up a trailing slash`() {
    assertEquals("$pillow/m/126x126", storyblokResized("$pillow/", 126))
  }

  @Test
  fun `leaves an already transformed url alone`() {
    val transformed = "$pillow/m/300x300"
    assertEquals(transformed, storyblokResized(transformed, 126))
  }

  @Test
  fun `leaves a non storyblok url alone`() {
    val other = "https://cdn.example.com/f/165473/832x832/hash/cat.png"
    assertEquals(other, storyblokResized(other, 126))
  }

  @Test
  fun `leaves the url alone when the target size is not yet known`() {
    assertEquals(pillow, storyblokResized(pillow, 0))
  }
}
