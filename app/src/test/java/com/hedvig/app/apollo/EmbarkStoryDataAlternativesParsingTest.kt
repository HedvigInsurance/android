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
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_MUTATION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_MUTATION_AND_SINGLE_VARIABLE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_QUERY_API_AND_GENERATED_VARIABLE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_QUERY_API_AND_SINGLE_VARIABLE
import org.junit.Test

@OptIn(ApolloExperimental::class)
class EmbarkStoryDataAlternativesParsingTest {
    private lateinit var mockServer: MockServer
    private lateinit var apolloClient: ApolloClient

    private suspend fun before() {
        mockServer = MockServer()
        apolloClient = ApolloClient.Builder().serverUrl(mockServer.url()).build()
    }

    private suspend fun after() {
        apolloClient.dispose()
        mockServer.stop()
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
}
