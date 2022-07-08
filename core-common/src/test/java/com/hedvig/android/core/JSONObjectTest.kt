package com.hedvig.android.core

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import org.json.JSONObject
import org.junit.Test

class JSONObjectTest {
  @Test
  fun `toJsonObject() should handle nested 'Map's`() {
    val map = mapOf(
      "foo" to mapOf(
        "bar" to mapOf(
          "baz" to "bat",
        ),
      ),
    )

    val expected = jsonObjectOf(
      "foo" to jsonObjectOf(
        "bar" to jsonObjectOf(
          "baz" to "bat",
        ),
      ),
    )

    val actual = map.toJsonObject()

    assertThat(actual.toString()).isEqualTo(expected.toString())
  }

  @Test
  fun `toJsonObject() should reject 'Map's with non-string keys`() {
    val map = mapOf(
      "asd" to "efg",
      1 to "foo",
    )

    assertThat { map.toJsonObject() }.isFailure().isInstanceOf(IllegalArgumentException::class)
  }

  @Test
  fun `toJsonObject() should handle 'List's`() {
    val expected = jsonObjectOf(
      "foo" to jsonArrayOf(1, 2, 3),
    )

    val actual = mapOf(
      "foo" to listOf(1, 2, 3),
    ).toJsonObject()

    assertThat(actual.toString(2)).isEqualTo(expected.toString(2))
  }

  @Test
  fun `jsonObjectOf() should handle nested 'Map's`() {
    val expected = JSONObject()

    val nested = JSONObject()
    nested.put("bar", "baz")

    expected.put("foo", nested)

    val actual = jsonObjectOf(
      "foo" to mapOf(
        "bar" to "baz",
      ),
    )

    assertThat(actual.toString()).isEqualTo(expected.toString())
  }

  @Test
  fun `jsonObjectOf() should handle 'List's`() {
    val expectedObject = JSONObject()
    expectedObject.put("foo", jsonArrayOf("1", "2", "3"))

    val expected = expectedObject["foo"]

    val actual = jsonObjectOf(
      "foo" to listOf("1", "2", "3"),
    )["foo"]

    assertThat(actual.javaClass).isEqualTo(expected.javaClass)
  }

  @Test
  fun `JSONObject asMap() should convert regular flat object`() {
    val expected = mapOf(
      "foo" to "bar",
      "bat" to "baz",
    )

    val actual = jsonObjectOf(
      "foo" to "bar",
      "bat" to "baz",
    ).asMap()

    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `JSONObject asMap() should convert nested object`() {
    val expected = mapOf(
      "foo" to "bar",
      "baz" to mapOf(
        "asd" to "efg",
      ),
    )

    val actual = jsonObjectOf(
      "foo" to "bar",
      "baz" to mapOf(
        "asd" to "efg",
      ),
    ).asMap()

    assertThat(actual).isEqualTo(expected)
  }
}
