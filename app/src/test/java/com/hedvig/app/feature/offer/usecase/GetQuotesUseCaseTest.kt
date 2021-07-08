package com.hedvig.app.feature.offer.usecase

import com.hedvig.android.owldroid.graphql.LastQuoteIdQuery
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.util.MockedApolloCall
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GetQuotesUseCaseTest {

    @Test
    fun `should find quote ID using lastQuoteOfMember when provided without ID`() {
        val mock = mockk<OfferRepository>(relaxed = true)
        coEvery { mock.quoteIdOfLastQuoteOfMember() } returns MockedApolloCall(
            LastQuoteIdQuery.Data(
                lastQuoteOfMember = LastQuoteIdQuery.LastQuoteOfMember(
                    asCompleteQuote = LastQuoteIdQuery.AsCompleteQuote(id = "123")
                )
            )
        )

        runBlocking {
            GetQuotesUseCase(mock)(emptyList())
            verify(exactly = 1) { mock.quoteIdOfLastQuoteOfMember() }
        }
    }

    @Test
    fun `should not attempt to load quote ID when provided with quote ID(s)`() {
        val mock = mockk<OfferRepository>(relaxed = true)

        coEvery { mock.quoteIdOfLastQuoteOfMember() } returns MockedApolloCall(
            LastQuoteIdQuery.Data(
                lastQuoteOfMember = LastQuoteIdQuery.LastQuoteOfMember(
                    asCompleteQuote = LastQuoteIdQuery.AsCompleteQuote(id = "123")
                )
            )
        )

        runBlocking {
            GetQuotesUseCase(mock)(listOf("123"))
            verify(exactly = 0) { mock.quoteIdOfLastQuoteOfMember() }
        }
    }
}
