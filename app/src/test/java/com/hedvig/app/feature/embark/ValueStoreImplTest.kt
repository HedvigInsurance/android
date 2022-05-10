package com.hedvig.app.feature.embark

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class ValueStoreImplTest {
    @Test
    fun `should extract MultiAction-item when 1 item is present`() {
        val store: ValueStore = ValueStoreImpl()

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
        val store: ValueStore = ValueStoreImpl()

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
        val store: ValueStore = ValueStoreImpl()

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
    fun `should clear store after double roll back, one to move them to staging, and one to clear staging too`() {
        val store: ValueStore = ValueStoreImpl()

        store.commitVersion() // Add another entry in the Stack to be able to roll back twice afterwards.
        store.put("foo", "bar")
        store.commitVersion()
        store.rollbackVersion()
        store.rollbackVersion()

        assertThat(store.get("foo")).isEqualTo(null)
    }

    @Test
    fun `null string value should result in null literal`() {
        val store: ValueStore = ValueStoreImpl()

        store.put("key[0]foo", "null")
        store.commitVersion()

        assertThat(store.get("key[0]foo")).isEqualTo(null)
    }

    @Test
    fun `should put things back in staging after rolling back`() {
        val store: ValueStore = ValueStoreImpl()

        store.commitVersion() // Add another entry in the Stack to be able to roll back twice afterwards.
        store.put("foo", "bar")
        store.commitVersion()
        store.rollbackVersion()
        assertThat(store.get("foo")).isEqualTo("bar")
        store.rollbackVersion()
        assertThat(store.get("foo")).isEqualTo(null)
    }

    @Test
    fun `withCommittedValues() doesn't affect the value store after rolling back`() {
        val store: ValueStore = ValueStoreImpl()

        store.put("foo", "bar")
        store.commitVersion()
        store.put("fooStage", "barStage")
        store.withCommittedVersion {
            assertThat(store.get("foo")).isEqualTo("bar")
            assertThat(store.get("fooStage")).isEqualTo("barStage")
        }
        assertThat(store.get("foo")).isEqualTo("bar")
        assertThat(store.get("fooStage")).isEqualTo("barStage")
    }

    @Test
    fun `rolling back correctly reverts staging to it's previous state`() {
        val store: ValueStore = ValueStoreImpl()

        store.put("foo", "bar")
        store.put("baz", "qux")
        store.commitVersion()
        store.put("baz", "quxCommitted")
        assertThat(store.get("foo")).isEqualTo("bar")
        assertThat(store.get("baz")).isEqualTo("qux") // We look at stage after storedValues on purpose.

        store.commitVersion()
        assertThat(store.get("foo")).isEqualTo("bar")
        assertThat(store.get("baz")).isEqualTo("quxCommitted")

        store.rollbackVersion()
        assertThat(store.get("foo")).isEqualTo("bar")
        assertThat(store.get("baz")).isEqualTo("qux")

        store.commitVersion()
        assertThat(store.get("foo")).isEqualTo("bar")
        assertThat(store.get("baz")).isEqualTo("quxCommitted")
    }
}
