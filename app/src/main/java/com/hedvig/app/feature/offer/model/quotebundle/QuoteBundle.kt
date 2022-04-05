package com.hedvig.app.feature.offer.model.quotebundle

import com.hedvig.android.owldroid.fragment.QuoteBundleFragment
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.table.Table
import com.hedvig.app.feature.table.intoTable
import java.time.LocalDate

data class QuoteBundle(
    val name: String,
    val quotes: List<Quote>,
    val cost: BundleCost,
    val frequentlyAskedQuestions: List<FrequentlyAskedQuestion>,
    val inception: Inception,
    val viewConfiguration: ViewConfiguration
) {
    data class Quote(
        val id: String,
        val dataCollectionId: String?,
        val displayName: String,
        val startDate: LocalDate?,
        val email: String?,
        val currentInsurer: CurrentInsurer?,
        val detailsTable: Table,
        val perils: List<Peril>,
        val insurableLimits: List<InsurableLimitItem.InsurableLimit>,
        val insuranceTerms: List<DocumentItems.Document>
    ) {
        data class CurrentInsurer(
            val switchable: Boolean,
            val name: String?
        )
    }

    data class FrequentlyAskedQuestion(
        val title: String?,
        val description: String?
    )

    fun hasCurrentInsurer() = quotes.any { it.currentInsurer != null }
    fun numberOfCurrentInsurers() = quotes.count { it.currentInsurer?.name != null }
}

fun QuoteBundleFragment.toQuoteBundle(quoteCartId: QuoteCartId?) = QuoteBundle(
    name = displayName,
    quotes = quotes.map { it.toQuote() },
    cost = toBundleCost(),
    frequentlyAskedQuestions = frequentlyAskedQuestions.map { it.toFrequentlyAskedQuestion() },
    inception = inception.toInception(
        appConfiguration.startDateTerminology,
        quoteCartId,
        quotes.map { it.displayName }
    ),
    viewConfiguration = appConfiguration.toViewConfiguration()
)

private fun QuoteBundleFragment.Quote.toQuote() = QuoteBundle.Quote(
    dataCollectionId = dataCollectionId,
    displayName = displayName,
    startDate = startDate,
    email = email,
    id = id,
    currentInsurer = currentInsurer?.toCurrentInsurer(),
    detailsTable = detailsTable.fragments.tableFragment.intoTable(),
    perils = contractPerils.map { Peril.from(it.fragments.perilFragment) },
    insurableLimits = insurableLimits.map {
        InsurableLimitItem.InsurableLimit.from(it.fragments.insurableLimitsFragment)
    },
    insuranceTerms = insuranceTerms.map {
        DocumentItems.Document.from(it.fragments.insuranceTermFragment)
    }
)

private fun QuoteBundleFragment.FrequentlyAskedQuestion.toFrequentlyAskedQuestion() =
    QuoteBundle.FrequentlyAskedQuestion(
        title = headline,
        description = body
    )

private fun QuoteBundleFragment.CurrentInsurer.toCurrentInsurer() = QuoteBundle.Quote.CurrentInsurer(
    switchable = switchable ?: false,
    name = displayName
)
