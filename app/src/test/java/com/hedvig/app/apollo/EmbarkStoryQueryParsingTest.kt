@file:OptIn(ApolloExperimental::class)

package com.hedvig.app.apollo

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.mockserver.MockServer
import com.apollographql.apollo3.mockserver.enqueue
import com.apollographql.apollo3.testing.runTest
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.testdata.feature.embark.data.STANDARD_STORY
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_MUTATION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_MUTATION_AND_SINGLE_VARIABLE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_QUERY_API_AND_GENERATED_VARIABLE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_QUERY_API_AND_SINGLE_VARIABLE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_INCOMPATIBLE_ACTION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_NUMBER_ACTION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_PASSED_KEY_VALUE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_SELECT_ACTION_API_MULTIPLE_OPTIONS
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_API
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_UNARY_EXPRESSIONS
import com.hedvig.app.util.apollo.adapter.CUSTOM_SCALAR_ADAPTERS
import org.junit.Test

class EmbarkStoryQueryParsingTest {
    private lateinit var mockServer: MockServer
    private lateinit var apolloClient: ApolloClient

    private suspend fun before() {
        mockServer = MockServer()
        apolloClient =
            ApolloClient.Builder().customScalarAdapters(CUSTOM_SCALAR_ADAPTERS).serverUrl(mockServer.url()).build()
    }

    private suspend fun after() {
        apolloClient.close()
        mockServer.stop()
    }

    @Test
    fun `apollo parses a standard story`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = STANDARD_STORY
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(EmbarkStoryQuery("", "sv_SE"))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }

    @Test
    fun `apollo parses a story with a graphql mutation`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = STORY_WITH_GRAPHQL_MUTATION
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(EmbarkStoryQuery("", "sv_SE"))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }

    @Test
    fun `apollo parses a story with a graphql mutation with a single variable`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = STORY_WITH_GRAPHQL_MUTATION_AND_SINGLE_VARIABLE
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(EmbarkStoryQuery("", "sv_SE"))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }

    @Test
    fun `apollo parses a story with a graphql query with a generated value`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = STORY_WITH_GRAPHQL_QUERY_API_AND_GENERATED_VARIABLE
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(EmbarkStoryQuery("", "sv_SE"))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }

    @Test
    fun `apollo parses a story with a graphql query with a single value`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = STORY_WITH_GRAPHQL_QUERY_API_AND_SINGLE_VARIABLE
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(EmbarkStoryQuery("", "sv_SE"))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }

    @Test
    fun `apollo parses a story with a graphql query with a number action`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = STORY_WITH_NUMBER_ACTION
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(EmbarkStoryQuery("", "sv_SE"))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }

    @Test
    fun `apollo parses a story with a graphql query with a passed key value`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = STORY_WITH_PASSED_KEY_VALUE
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(EmbarkStoryQuery("", "sv_SE"))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }

    @Test
    fun `apollo parses a story with a graphql query with a select action with multiple options`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = STORY_WITH_SELECT_ACTION_API_MULTIPLE_OPTIONS
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(EmbarkStoryQuery("", "sv_SE"))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }

    @Test
    fun `apollo parses a story with a graphql query text action api`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = STORY_WITH_TEXT_ACTION_API
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(EmbarkStoryQuery("", "sv_SE"))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }

    @Test
    fun `apollo parses a story with a message builder with expressions inside it`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = STORY_WITH_UNARY_EXPRESSIONS
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(EmbarkStoryQuery("", "sv_SE"))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }

    @Test
    fun `apollo parses a story with an incompatible action`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = STORY_WITH_INCOMPATIBLE_ACTION
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(EmbarkStoryQuery("", "sv_SE"))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }
}
