package com.hedvig.app.feature.embark.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.createAndAddWithLodashNotation
import org.json.JSONObject
import org.junit.Test

class LodashSetTest {

    @Test
    fun `should insert json value`() {
        val json = JSONObject("{}")
        val key = "address"
        val value = "Hello World"
        val result = json.createAndAddWithLodashNotation(value, key, key.substringBefore("."))

        assertThat(result.toString()).isEqualTo(JSONObject("{ \"address\": \"Hello World\" }").toString())
    }

    @Test
    fun `should insert json into nested key`() {
        val json = JSONObject("{}")
        val key = "input.address"
        val value = "Hello World"
        json.createAndAddWithLodashNotation(value, key, key.substringBefore("."))

        assertThat(json.toString())
            .isEqualTo(JSONObject("{\"input\":{\"address\":\"Hello World\"}}").toString())
    }

    @Test
    fun `should insert json into nested key with partial JSON`() {
        val json = JSONObject("{\"input\":{\"other\":\"value\"}}")
        val key = "input.address.data"
        val value = "Hello World"
        json.createAndAddWithLodashNotation(value, key, key.substringBefore("."))

        assertThat(json.toString())
            .isEqualTo(
                JSONObject("{\"input\":{\"other\":\"value\", \"address\":{\"data\":\"Hello World\"}}}").toString(),
            )
    }

    @Test
    fun `should insert json into array`() {
        val json = JSONObject("{}")
        val key = "input.types[0].data"
        val value = "Hello World"
        json.createAndAddWithLodashNotation(value, key, key.substringBefore("."))

        assertThat(json.toString())
            .isEqualTo(JSONObject("{\"input\":{\"types\":[{\"data\":\"Hello World\"}]}}").toString())
    }
}
