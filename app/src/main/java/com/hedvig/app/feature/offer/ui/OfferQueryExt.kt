package com.hedvig.app.feature.offer.ui

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.util.apollo.toMonetaryAmount

fun OfferQuery.Data.netMonthlyCost() = quoteBundle
    .bundleCost
    .fragments
    .costFragment
    .monthlyNet
    .fragments
    .monetaryAmountFragment
    .toMonetaryAmount()

fun OfferQuery.Data.grossMonthlyCost() = quoteBundle
    .bundleCost
    .fragments
    .costFragment
    .monthlyGross
    .fragments
    .monetaryAmountFragment
    .toMonetaryAmount()
