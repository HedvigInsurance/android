package com.hedvig.app.feature.offer.usecase

import com.hedvig.android.owldroid.graphql.LastQuoteIdQuery
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.usecase.getquote.GetQuotesUseCase
import com.hedvig.app.util.MockedApolloCall
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GetQuotesUseCaseTest {

    @Test
    fun `should find quote ID using lastQuoteOfMember when provided without ID`() {
        val mockOfferRepository = mockk<OfferRepository>(relaxed = true)
        coEvery { mockOfferRepository.quoteIdOfLastQuoteOfMember() } returns MockedApolloCall(
            LastQuoteIdQuery.Data(
                lastQuoteOfMember = LastQuoteIdQuery.LastQuoteOfMember(
                    asCompleteQuote = LastQuoteIdQuery.AsCompleteQuote(id = "123")
                )
            )
        )

        val mockFeatureManager = mockk<FeatureManager>()
        every {
            mockFeatureManager.isFeatureEnabled(Feature.QUOTE_CART)
        } returns false

        runBlocking {
            GetQuotesUseCase(mockOfferRepository, mockFeatureManager)(emptyList())
            verify(exactly = 1) { mockOfferRepository.quoteIdOfLastQuoteOfMember() }
        }
    }

    @Test
    fun `should not attempt to load quote ID when provided with quote ID(s)`() {
        val mockOfferRepository = mockk<OfferRepository>(relaxed = true)

        coEvery { mockOfferRepository.quoteIdOfLastQuoteOfMember() } returns MockedApolloCall(
            LastQuoteIdQuery.Data(
                lastQuoteOfMember = LastQuoteIdQuery.LastQuoteOfMember(
                    asCompleteQuote = LastQuoteIdQuery.AsCompleteQuote(id = "123")
                )
            )
        )

        val mockFeatureManager = mockk<FeatureManager>()

        runBlocking {
            GetQuotesUseCase(mockOfferRepository, mockFeatureManager)(listOf("123"))
            verify(exactly = 0) { mockOfferRepository.quoteIdOfLastQuoteOfMember() }
        }
    }
}
