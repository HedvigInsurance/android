package com.hedvig.app.feature.offer

import com.hedvig.app.util.CoroutinesTestRule
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class OfferViewModelImplTest {

    @get:Rule
    val coroutinesRule = CoroutinesTestRule()

    @Test
    fun `should find quote ID using lastQuoteOfMember when created without ID`() {
        val mock = mockk<OfferRepository>(relaxed = true)

        OfferViewModelImpl(emptyList(), mock)

        verify(exactly = 1) { mock.quoteIdOfLastQuoteOfMember() }
    }

    @Test
    fun `should not attempt to load quote ID when created with quote ID(s)`() {
        val mock = mockk<OfferRepository>(relaxed = true)

        OfferViewModelImpl(listOf("123"), mock)

        verify(exactly = 0) { mock.quoteIdOfLastQuoteOfMember() }
    }
}
