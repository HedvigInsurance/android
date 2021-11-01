package com.hedvig.app.feature.embark

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class ValueStoreImplTest {
    @Test
    fun `should extract MultiAction-item when 1 item is present`() {
        val store = ValueStoreImpl()

        store.put("key[0]foo", "bar")
        store.put("key[0]baz", "bat")
        store.commitVersion()

        assertThat(store.getMultiActionItems("key")).isEqualTo(
            listOf(
                mapOf(
                    "foo" to "bar",
                    "baz" to "bat",
                )
            )
        )
    }

    @Test
    fun `should extract MultiAction-item when 2 items are present`() {
        val store = ValueStoreImpl()

        store.put("key[0]foo", "bar")
        store.put("key[0]baz", "bat")
        store.put("key[1]foo", "car")
        store.put("key[1]baz", "cat")
        store.commitVersion()

        assertThat(store.getMultiActionItems("key")).isEqualTo(
            listOf(
                mapOf(
                    "foo" to "bar",
                    "baz" to "bat",
                ),
                mapOf(
                    "foo" to "car",
                    "baz" to "cat",
                )
            )
        )
    }

    @Test
    fun `should extract MultiAction-item from stage`() {
        val store = ValueStoreImpl()

        store.put("key[0]foo", "bar")
        store.put("key[0]baz", "bat")
        assertThat(store.getMultiActionItems("key")).isEqualTo(
            listOf(
                mapOf(
                    "foo" to "bar",
                    "baz" to "bat",
                )
            )
        )
    }

    @Test
    fun `should clear store after roll back`() {
        val store = ValueStoreImpl()

        store.put("foo", "bar")
        store.commitVersion()
        store.rollbackVersion()

        assertThat(store.get("foo")).isEqualTo(null)
    }
}
