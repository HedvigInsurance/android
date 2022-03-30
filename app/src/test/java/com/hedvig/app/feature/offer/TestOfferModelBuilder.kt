package com.hedvig.app.feature.offer

import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.model.Campaign
import com.hedvig.app.feature.offer.model.CheckoutLabel
import com.hedvig.app.feature.offer.model.CheckoutMethod
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.quotebundle.BundleCost
import com.hedvig.app.feature.offer.model.quotebundle.GradientType
import com.hedvig.app.feature.offer.model.quotebundle.Inception
import com.hedvig.app.feature.offer.model.quotebundle.OfferStartDate
import com.hedvig.app.feature.offer.model.quotebundle.PostSignScreen
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.model.quotebundle.StartDateLabel
import com.hedvig.app.feature.offer.model.quotebundle.ViewConfiguration
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.table.Table
import org.javamoney.moneta.Money
import java.time.LocalDate
import javax.money.MonetaryAmount

class TestOfferModelBuilder(
    private val bundleName: String = "Bundle Display Name",
    private val quoteName: String = "Test Quote",
    private val startDate: LocalDate = LocalDate.of(20122, 2, 21),
    private val email: String = "test@test.com",
    private val id: String = "ea656f5f-40b2-4953-85d9-752b33e69e38",
    private val table: Table = Table(
        "Test Table",
        listOf(
            Table.Section(
                "Section 1",
                listOf(
                    Table.Row("Row 1", "Subtitle 1", "Value 1")
                )
            )
        )
    ),
    private val perils: List<Peril> = listOf(
        Peril(
            "Test peril 1",
            "Test description 1",
            "dark url test",
            "light url test",
            emptyList(),
            emptyList(),
            "Test info"
        )
    ),
    private val insurableLimits: List<InsurableLimitItem.InsurableLimit> = listOf(
        InsurableLimitItem.InsurableLimit(
            "Test limit",
            "test",
            "testable limit"
        )
    ),
    private val documents: List<DocumentItems.Document> = emptyList(),
    private val grossMonthlyCost: MonetaryAmount = Money.parse("SEK 200"),
    private val netMonthlyCost: MonetaryAmount = Money.parse("SEK 50"),
    private val frequentlyAskedQuestions: List<QuoteBundle.FrequentlyAskedQuestion> = listOf(
        QuoteBundle.FrequentlyAskedQuestion(
            "Question 1",
            "Test question"
        )
    ),
    private val inception: Inception = Inception(
        startDate = OfferStartDate.AtDate(LocalDate.of(2022, 2, 21)),
        startDateLabel = StartDateLabel.SINGLE_START_DATE,
        changeDateData = ChangeDateBottomSheetData(emptyList())
    ),
    private val viewConfiguration: ViewConfiguration = ViewConfiguration(
        showCampaignManagement = true,
        showFAQ = true,
        ignoreCampaigns = false,
        title = ViewConfiguration.Title.LOGO,
        startDateTerminology = ViewConfiguration.StartDateTerminology.START_DATE,
        gradient = GradientType.FALL_SUNSET,
        postSignScreen = PostSignScreen.CONNECT_PAYIN
    ),
    private val checkoutMethod: CheckoutMethod = CheckoutMethod.SWEDISH_BANK_ID,
    private val checkoutLabel: CheckoutLabel = CheckoutLabel.SIGN_UP,
    private val campaign: Campaign? = null,
    private val ignoreCampaigns: Boolean = false,
) {

    fun build() = OfferModel(
        quoteBundle = QuoteBundle(
            name = bundleName,
            quotes = listOf(
                QuoteBundle.Quote(
                    dataCollectionId = null,
                    displayName = quoteName,
                    startDate = startDate,
                    email = email,
                    id = id,
                    currentInsurer = null,
                    detailsTable = table,
                    perils = perils,
                    insurableLimits = insurableLimits,
                    insuranceTerms = documents
                )
            ),
            cost = BundleCost(
                grossMonthlyCost = grossMonthlyCost,
                netMonthlyCost = netMonthlyCost,
                ignoreCampaigns = ignoreCampaigns
            ),
            frequentlyAskedQuestions = frequentlyAskedQuestions,
            inception = inception,
            viewConfiguration = viewConfiguration
        ),
        checkoutMethod = checkoutMethod,
        checkoutLabel = checkoutLabel,
        campaign = campaign,
        checkout = null
    )
}
