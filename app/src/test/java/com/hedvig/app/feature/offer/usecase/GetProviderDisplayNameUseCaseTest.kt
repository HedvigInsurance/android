package com.hedvig.app.feature.offer.usecase

import assertk.assertThat
import assertk.assertions.isNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ProviderStatusQuery
import com.hedvig.app.feature.offer.usecase.providerstatus.GetProviderDisplayNameUseCase
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Ignore
import org.junit.Test

class GetProviderDisplayNameUseCaseTest {

    private val mockedApolloClient = mockk<ApolloClient>().apply {
        val apolloClient = this
        coEvery { apolloClient.query(ProviderStatusQuery()).safeQuery() } coAnswers {
            QueryResult.Success(
                ProviderStatusQuery.Data(
                    ProviderStatusQuery.ExternalInsuranceProvider(
                        providerStatusV2 = listOf(
                            ProviderStatusQuery.ProviderStatusV2("", "se-trygg-hansa", "Trygg-Hansa"),
                            ProviderStatusQuery.ProviderStatusV2("", "se-folksam", "Folksam"),
                            ProviderStatusQuery.ProviderStatusV2("", "se-if", "If"),
                            ProviderStatusQuery.ProviderStatusV2("", "se-lansforsakringar", "Länsförsäkringar"),
                            ProviderStatusQuery.ProviderStatusV2("", "se-moderna", "Moderna"),
                            ProviderStatusQuery.ProviderStatusV2("", "se-dina", "Dina"),
                            ProviderStatusQuery.ProviderStatusV2("", "se-ica", "ICA"),
                            ProviderStatusQuery.ProviderStatusV2("", "se-gjensidige", "Gjensidige"),
                            ProviderStatusQuery.ProviderStatusV2("", "se-trekronor", "Trekronor"),
                            ProviderStatusQuery.ProviderStatusV2("", "no-demo", "Demo"),
                            ProviderStatusQuery.ProviderStatusV2("", "se-demo", "Demo")
                        )
                    )
                )
            )
        }
    }
    private val useCase = GetProviderDisplayNameUseCase(mockedApolloClient)

    @Test
    @Ignore("Ignore until apolloClient is mocked properly")
    fun `when sending an unrelated insuranceCompany code string, a null is sent back`() = runBlockingTest {
        assertThat { useCase("random unrelated text") }.isNull()
    }

    @Test
    @Ignore("Ignore until apolloClient is mocked properly")
    fun `with a prefixed insurance code name, the company display name is returned back`() = runBlockingTest {
        assert(useCase("se-ica") == "ICA")
        assert(useCase("se-trygg-hansa") == "Trygg-Hansa")
        assert(useCase("se-folksam") == "Folksam")
        assert(useCase("se-if") == "If")
        assert(useCase("se-lansforsakringar") == "Länsförsäkringar")
        assert(useCase("se-moderna") == "Moderna")
        assert(useCase("se-dina") == "Dina")
        assert(useCase("se-ica") == "ICA")
        assert(useCase("se-gjensidige") == "Gjensidige")
        assert(useCase("se-trekronor") == "Trekronor")
        assert(useCase("no-demo") == "Demo")
        assert(useCase("se-demo") == "Demo")
    }
}
