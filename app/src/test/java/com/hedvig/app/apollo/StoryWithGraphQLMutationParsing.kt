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
import com.hedvig.app.testdata.feature.embark.data.VARIABLE_MUTATION
import com.hedvig.app.util.jsonObjectOf
import org.junit.Test

@OptIn(ApolloExperimental::class)
class StoryWithGraphQLMutationParsing {
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
    fun `apollo handles a Data object constructed with its constructor, providing the necessary typename`() = runTest(
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
    fun `2`() = runTest(
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
}
