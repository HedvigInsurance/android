package com.hedvig.app.feature.offer.quotedetail

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.perils.PerilItem

fun buildPerils(quote: OfferQuery.Quote) = quote
    .contractPerils
    .map { PerilItem.Peril(Peril.from(it.fragments.perilFragment)) }

fun buildInsurableLimits(quote: OfferQuery.Quote) = quote
    .insurableLimits
    .map {
        InsurableLimitItem.InsurableLimit.from(it.fragments.insurableLimitsFragment)
    }

fun buildDocuments(quote: OfferQuery.Quote) = quote
    .insuranceTerms
    .map { DocumentItems.Document.from(it) }
