@file:OptIn(ApolloExperimental::class)

package com.hedvig.app.feature.offer.usecase

import assertk.assertThat
import assertk.assertions.isNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.mockserver.MockServer
import com.apollographql.apollo3.mockserver.enqueue
import com.apollographql.apollo3.testing.runTest
import com.hedvig.android.owldroid.graphql.ProviderStatusQuery
import com.hedvig.app.apollo.toJsonStringWithData
import com.hedvig.app.feature.offer.usecase.providerstatus.GetProviderDisplayNameUseCase
import org.junit.Test

class GetProviderDisplayNameUseCaseTest {
    private lateinit var mockServer: MockServer
    private lateinit var apolloClient: ApolloClient

    private suspend fun before() {
        mockServer = MockServer()
        apolloClient = ApolloClient
            .Builder()
            .normalizedCache(MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024))
            .serverUrl(mockServer.url())
            .build()
    }

    private suspend fun after() {
        apolloClient.close()
        mockServer.stop()
    }

    private val mockedResponse: ProviderStatusQuery.Data = ProviderStatusQuery.Data(
        ProviderStatusQuery.ExternalInsuranceProvider(
            providerStatusV2 = listOf(
                ProviderStatusQuery.ProviderStatusV2("se-trygg-hansa", "Trygg-Hansa"),
                ProviderStatusQuery.ProviderStatusV2("se-folksam", "Folksam"),
                ProviderStatusQuery.ProviderStatusV2("se-if", "If"),
                ProviderStatusQuery.ProviderStatusV2("se-lansforsakringar", "Länsförsäkringar"),
                ProviderStatusQuery.ProviderStatusV2("se-moderna", "Moderna"),
                ProviderStatusQuery.ProviderStatusV2("se-dina", "Dina"),
                ProviderStatusQuery.ProviderStatusV2("se-ica", "ICA"),
                ProviderStatusQuery.ProviderStatusV2("se-gjensidige", "Gjensidige"),
                ProviderStatusQuery.ProviderStatusV2("se-trekronor", "Trekronor"),
                ProviderStatusQuery.ProviderStatusV2("no-demo", "Demo"),
                ProviderStatusQuery.ProviderStatusV2("se-demo", "Demo")
            )
        )
    )

    @Test
    fun `when sending an unrelated insuranceCompany code string, a null is sent back`() = runTest(
        before = { before() },
        after = { after() },
    ) {
        val useCase = GetProviderDisplayNameUseCase(apolloClient)
        mockServer.enqueue(mockedResponse.toJsonStringWithData())

        assertThat(useCase.invoke("random unrelated text")).isNull()
    }

    @Test
    fun `with a prefixed insurance code name, the company display name is returned back`() = runTest(
        before = { before() },
        after = { after() },
    ) {
        val useCase = GetProviderDisplayNameUseCase(apolloClient)
        mockServer.enqueue(mockedResponse.toJsonStringWithData())

        assert(useCase.invoke("se-ica") == "ICA")
        assert(useCase.invoke("se-trygg-hansa") == "Trygg-Hansa")
        assert(useCase.invoke("se-folksam") == "Folksam")
        assert(useCase.invoke("se-if") == "If")
        assert(useCase.invoke("se-lansforsakringar") == "Länsförsäkringar")
        assert(useCase.invoke("se-moderna") == "Moderna")
        assert(useCase.invoke("se-dina") == "Dina")
        assert(useCase.invoke("se-ica") == "ICA")
        assert(useCase.invoke("se-gjensidige") == "Gjensidige")
        assert(useCase.invoke("se-trekronor") == "Trekronor")
        assert(useCase.invoke("no-demo") == "Demo")
        assert(useCase.invoke("se-demo") == "Demo")
    }
}
