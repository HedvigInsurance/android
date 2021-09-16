package com.hedvig.app.feature.offer.ui

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationApproveButtonTerminology
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.R
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

fun OfferQuery.Data.checkoutTextRes() = when (signMethodForQuotes) {
    SignMethod.SWEDISH_BANK_ID -> R.string.OFFER_SIGN_BUTTON
    SignMethod.SIMPLE_SIGN -> R.string.OFFER_CHECKOUT_BUTTON
    SignMethod.APPROVE_ONLY -> when (this.quoteBundle.appConfiguration.approveButtonTerminology) {
        QuoteBundleAppConfigurationApproveButtonTerminology.APPROVE_CHANGES -> R.string.OFFER_APPROVE_CHANGES
        QuoteBundleAppConfigurationApproveButtonTerminology.CONFIRM_PURCHASE -> R.string.OFFER_CONFIRM_PURCHASE
        QuoteBundleAppConfigurationApproveButtonTerminology.UNKNOWN__ -> null
    }
    SignMethod.NORWEGIAN_BANK_ID, // Deprecated
    SignMethod.DANISH_BANK_ID, // Deprecated
    SignMethod.UNKNOWN__ -> null
}
