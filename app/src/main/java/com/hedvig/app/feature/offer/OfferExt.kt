package com.hedvig.app.feature.offer

import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.OfferQuery

fun Response<OfferQuery.Data>.priceOrNull() = data
    ?.lastQuoteOfMember
    ?.asCompleteQuote
    ?.insuranceCost
    ?.fragments
    ?.costFragment
    ?.monthlyNet
    ?.fragments
    ?.monetaryAmountFragment
    ?.amount
    ?.toBigDecimal()
    ?.toDouble()
