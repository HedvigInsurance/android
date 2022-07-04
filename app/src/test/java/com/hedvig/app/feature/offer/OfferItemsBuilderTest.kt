package com.hedvig.app.feature.offer

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.app.feature.offer.model.CheckoutLabel
import com.hedvig.app.feature.offer.model.CheckoutMethod
import com.hedvig.app.feature.offer.ui.OfferItems
import com.hedvig.app.util.containsNoneOfType
import com.hedvig.app.util.containsOfType
import org.javamoney.moneta.Money
import org.junit.Test

class OfferItemsBuilderTest {

    @Test
    fun `should set gross price as premium when ignore campaigns is true`() {
        val monthlyCost = Money.of(300, "SEK")
        val testData = TestOfferModelBuilder(
            grossMonthlyCost = monthlyCost,
            ignoreCampaigns = true,
        ).build()

        val items = OfferItemsBuilder.createTopOfferItems(
            testData.variants.first(),
            null,
            null,
            {},
            testData,
        )

        assertThat(items).containsOfType<OfferItems.Header>()
        val header = items.first { it is OfferItems.Header } as OfferItems.Header
        assertThat(header.premium).isEqualTo(monthlyCost)
    }

    @Test
    fun `should set net price as premium when ignore campaigns is false`() {
        val netMonthlyCost = Money.of(310, "SEK")
        val testData = TestOfferModelBuilder(
            netMonthlyCost = netMonthlyCost,
            ignoreCampaigns = false,
        ).build()

        val items = OfferItemsBuilder.createTopOfferItems(
            testData.variants.first(),
            null,
            null,
            {},
            testData,
        )

        assertThat(items).containsOfType<OfferItems.Header>()
        val header = items.first { it is OfferItems.Header } as OfferItems.Header
        assertThat(header.premium).isEqualTo(netMonthlyCost)
    }

    @Test
    fun `should set correct checkout text for approve changes`() {
        val testData = TestOfferModelBuilder(
            checkoutMethod = CheckoutMethod.APPROVE_ONLY,
            checkoutLabel = CheckoutLabel.APPROVE,
        ).build()

        val items = OfferItemsBuilder.createTopOfferItems(
            testData.variants.first(),
            null,
            null,
            {},
            testData,
        )

        assertThat(items).containsOfType<OfferItems.Header>()
        val header = items.first { it is OfferItems.Header } as OfferItems.Header
        assertThat(header.checkoutLabel).isEqualTo(CheckoutLabel.APPROVE)

        val bottomItems = OfferItemsBuilder.createBottomOfferItems(testData.variants.first())

        assertThat(bottomItems).containsOfType<OfferItems.Footer>()
        val footer = bottomItems.first { it is OfferItems.Footer } as OfferItems.Footer
        assertThat(footer.checkoutLabel).isEqualTo(CheckoutLabel.APPROVE)
    }

    @Test
    fun `should set correct checkout text for confirm purchase`() {
        val testData = TestOfferModelBuilder(
            checkoutMethod = CheckoutMethod.APPROVE_ONLY,
            checkoutLabel = CheckoutLabel.CONFIRM,
        ).build()

        val topItems = OfferItemsBuilder.createTopOfferItems(
            testData.variants.first(),
            null,
            null,
            {},
            testData,
        )

        assertThat(topItems).containsOfType<OfferItems.Header>()
        val header = topItems.first { it is OfferItems.Header } as OfferItems.Header
        assertThat(header.checkoutLabel).isEqualTo(CheckoutLabel.CONFIRM)

        val bottomItems = OfferItemsBuilder.createBottomOfferItems(testData.variants.first())

        assertThat(bottomItems).containsOfType<OfferItems.Footer>()
        val footer = bottomItems.first { it is OfferItems.Footer } as OfferItems.Footer
        assertThat(footer.checkoutLabel).isEqualTo(CheckoutLabel.CONFIRM)
    }

    @Test
    fun `should show variant picker when there is more than one variant available in the offer`() {
        val testData = TestOfferModelBuilder(
            checkoutMethod = CheckoutMethod.APPROVE_ONLY,
            checkoutLabel = CheckoutLabel.CONFIRM,
            numberOfVariants = 2,
        ).build()

        val topItems = OfferItemsBuilder.createTopOfferItems(
            testData.variants.first(),
            null,
            null,
            {},
            testData,
        )

        assertThat(topItems).all {
            containsOfType<OfferItems.VariantHeader>()
            containsOfType<OfferItems.VariantButton>(2)
        }
    }

    @Test
    fun `should not show variant picker when there only one variant available in the offer`() {
        val testData = TestOfferModelBuilder(
            checkoutMethod = CheckoutMethod.APPROVE_ONLY,
            checkoutLabel = CheckoutLabel.CONFIRM,
            numberOfVariants = 1,
        ).build()

        val topItems = OfferItemsBuilder.createTopOfferItems(
            testData.variants.first(),
            null,
            null,
            {},
            testData,
        )

        assertThat(topItems).all {
            containsNoneOfType<OfferItems.VariantHeader>()
            containsNoneOfType<OfferItems.VariantButton>()
        }
    }
}
