package com.hedvig.app.util

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
                    "baz" to "bat"
                )
            )
        )

        val expected = jsonObjectOf(
            "foo" to jsonObjectOf(
                "bar" to jsonObjectOf(
                    "baz" to "bat"
                )
            )
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
    fun `jsonObjectOf() should handle nested 'Map's`() {
        val expected = JSONObject()

        val nested = JSONObject()
        nested.put("bar", "baz")

        expected.put("foo", nested)

        val actual = jsonObjectOf(
            "foo" to mapOf(
                "bar" to "baz"
            )
        )

        assertThat(actual.toString()).isEqualTo(expected.toString())
    }
}
