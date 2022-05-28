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
import com.hedvig.android.owldroid.graphql.test.EmbarkStoryQuery_TestBuilder.Data
import com.hedvig.app.testdata.feature.embark.data.STANDARD_STORY
import org.junit.Test

@OptIn(ApolloExperimental::class)
class EmbarkStoryQueryParsing {
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

    @Suppress("PrivatePropertyName")
    private val EMBARK_STORY_from_test_builder by lazy {
        EmbarkStoryQuery.Data {
            this.embarkStory = this.embarkStory {
                this.startPassage = "1"
                this.computedStoreValues = emptyList()
                this.passages = listOf(
                    this.passage {
                        this.id = "1"
                        this.name = "TestPassage"
                        this.messages = listOf(
                            this.message {
                                this.text = "test message"
                                this.expressions = emptyList()
                            }
                        )
                        this.response = this.embarkMessageResponse {
                            this.text = "{TestPassageResult}"
                            this.expressions = emptyList()
                        }
                        this.action = this.embarkSelectActionAction {
                            this.selectData = this.selectData {
                                this.options = listOf(
                                    this.option {
                                        this.link = this.link {
                                            this.name = "TestPassage2"
                                            this.label = "Another test passage"
                                        }
                                        this.badge = "Badge #1"
                                        this.api = null
                                        this.keys = emptyList()
                                        this.values = emptyList()
                                    },
                                    this.option {
                                        this.link = this.link {
                                            this.name = "TestPassage"
                                            this.label = "Yet another test passage"
                                        }
                                        this.badge = "Badge #2"
                                        this.api = null
                                        this.keys = emptyList()
                                        this.values = emptyList()
                                    },
                                    this.option {
                                        this.link = this.link {
                                            this.name = "TestPassage2"
                                            this.label = "Another test passage"
                                        }
                                        this.badge = null
                                        this.api = null
                                        this.keys = emptyList()
                                        this.values = emptyList()
                                    }
                                )
                            }
                        }
                        this.redirects = emptyList()
                        this.quoteCartOfferRedirects = emptyList()
                        this.variantedOfferRedirects = emptyList()
                        this.api = null
                        this.tracks = emptyList()
                        this.externalRedirect = null
                        this.offerRedirect = null
                        this.tooltips = emptyList()
                        this.allLinks = emptyList()
                    },
                    this.passage {
                        this.id = "2"
                        this.name = "TestPassage2"
                        this.messages = listOf(
                            this.message {
                                this.text = "another test message"
                                this.expressions = emptyList()
                            }
                        )
                        this.response = this.embarkMessageResponse {
                            this.text = ""
                            this.expressions = emptyList()
                        }
                        this.action = this.embarkSelectActionAction {
                            this.selectData = this.selectData {
                                this.options = listOf(
                                    this.option {
                                        this.link = this.link {
                                            this.name = "TestPassage"
                                            this.label = "Yet another test passage"
                                        }
                                        this.api = null
                                        this.badge = null
                                        this.keys = emptyList()
                                        this.values = emptyList()
                                    },
                                    this.option {
                                        this.link = this.link {
                                            this.name = "TestPassage"
                                            this.label = "Yet another test passage"
                                        }
                                        this.api = null
                                        this.badge = null
                                        this.keys = emptyList()
                                        this.values = emptyList()
                                    },
                                )
                            }
                        }
                        this.redirects = emptyList()
                        this.quoteCartOfferRedirects = emptyList()
                        this.variantedOfferRedirects = emptyList()
                        this.api = null
                        this.tracks = emptyList()
                        this.externalRedirect = null
                        this.offerRedirect = null
                        this.tooltips = emptyList()
                        this.allLinks = emptyList()
                    }
                )
            }
        }
    }

    @Test
    fun `apollo correctly parses data object constructed using apollo test builders`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = EMBARK_STORY_from_test_builder
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(EmbarkStoryQuery("", "sv_SE"))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }

    @Test
    fun `apollo handles a Data object constructed with its constructor, providing the necessary typename`() = runTest(
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
}
