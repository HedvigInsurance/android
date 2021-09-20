package com.hedvig.app.feature.offer

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.CheckoutLabel
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.testdata.common.builders.CostBuilder
import com.hedvig.app.testdata.feature.offer.builders.AppConfigurationBuilder
import com.hedvig.app.testdata.feature.offer.builders.OfferDataBuilder
import com.hedvig.app.util.containsOfType
import org.javamoney.moneta.Money
import org.junit.Test

class OfferItemsBuilderTest {

    @Test
    fun `should set gross price as premium when ignore campaigns is true`() {
        val grossAmount = "300.00"
        val currency = "SEK"

        val testData = OfferDataBuilder(
            insuranceCost = CostBuilder(
                netAmount = "350.00",
                grossAmount = grossAmount,
                currency = currency
            ).build(),
            appConfiguration = AppConfigurationBuilder(ignoreCampaigns = true).build()
        ).build()

        val items = OfferItemsBuilder.createTopOfferItems(testData)

        assertThat(items).containsOfType<OfferModel.Header>()
        val header = items.first { it is OfferModel.Header } as OfferModel.Header
        assertThat(header.premium).isEqualTo(Money.of(grossAmount.toBigDecimal(), currency))
    }

    @Test
    fun `should set net price as premium when ignore campaigns is false`() {
        val netAmount = "310.00"
        val currency = "SEK"

        val testData = OfferDataBuilder(
            insuranceCost = CostBuilder(
                netAmount = netAmount,
                grossAmount = "350.00",
                currency = currency
            ).build(),
            appConfiguration = AppConfigurationBuilder(ignoreCampaigns = false).build()
        ).build()

        val items = OfferItemsBuilder.createTopOfferItems(testData)

        assertThat(items).containsOfType<OfferModel.Header>()
        val header = items.first { it is OfferModel.Header } as OfferModel.Header
        assertThat(header.premium).isEqualTo(Money.of(netAmount.toBigDecimal(), currency))
    }

    @Test
    fun `should set correct checkout text for approve changes`() {
        val testData = OfferDataBuilder(
            signMethod = SignMethod.APPROVE_ONLY,
            appConfiguration = AppConfigurationBuilder(ignoreCampaigns = false).build()
        ).build()

        val items = OfferItemsBuilder.createTopOfferItems(testData)

        assertThat(items).containsOfType<OfferModel.Header>()
        val header = items.first { it is OfferModel.Header } as OfferModel.Header
        assertThat(header.checkoutLabel).isEqualTo(CheckoutLabel.APPROVE)

        val bottomItems = OfferItemsBuilder.createBottomOfferItems(testData)

        assertThat(bottomItems).containsOfType<OfferModel.Footer>()
        val footer = bottomItems.first { it is OfferModel.Footer } as OfferModel.Footer
        assertThat(footer.checkoutLabel).isEqualTo(CheckoutLabel.APPROVE)
    }

    @Test
    fun `should set correct checkout text for confirm purchase`() {
        val testData = OfferDataBuilder(
            signMethod = SignMethod.APPROVE_ONLY,
            appConfiguration = AppConfigurationBuilder(ignoreCampaigns = true).build()
        ).build()

        val topItems = OfferItemsBuilder.createTopOfferItems(testData)

        assertThat(topItems).containsOfType<OfferModel.Header>()
        val header = topItems.first { it is OfferModel.Header } as OfferModel.Header
        assertThat(header.checkoutLabel).isEqualTo(CheckoutLabel.CONFIRM)

        val bottomItems = OfferItemsBuilder.createBottomOfferItems(testData)

        assertThat(bottomItems).containsOfType<OfferModel.Footer>()
        val footer = bottomItems.first { it is OfferModel.Footer } as OfferModel.Footer
        assertThat(footer.checkoutLabel).isEqualTo(CheckoutLabel.CONFIRM)
    }
}
