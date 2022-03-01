package com.hedvig.app.feature.offer.quotedetail

import com.hedvig.android.owldroid.fragment.QuoteBundleFragment
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.perils.PerilItem

fun buildPerils(quote: QuoteBundleFragment.Quote) = quote
    .contractPerils
    .map { PerilItem.Peril(Peril.from(it.fragments.perilFragment)) }

fun buildInsurableLimits(quote: QuoteBundleFragment.Quote) = quote
    .insurableLimits
    .map {
        InsurableLimitItem.InsurableLimit.from(it.fragments.insurableLimitsFragment)
    }

fun buildDocuments(quote: QuoteBundleFragment.Quote) = quote
    .insuranceTerms
    .map { DocumentItems.Document.from(it.fragments.insuranceTermFragment) }
