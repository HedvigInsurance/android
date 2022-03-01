package com.hedvig.app.feature.offer.ui

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.QuoteCartSubscription
import com.hedvig.android.owldroid.type.CheckoutMethod
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationApproveButtonTerminology
import com.hedvig.android.owldroid.type.SignMethod

fun OfferQuery.Data.checkoutLabel() = when (signMethodForQuotes) {
    SignMethod.SWEDISH_BANK_ID -> CheckoutLabel.SIGN_UP
    SignMethod.SIMPLE_SIGN -> CheckoutLabel.CONTINUE
    SignMethod.APPROVE_ONLY -> when (
        this.quoteBundle.fragments.quoteBundleFragment
            .appConfiguration.approveButtonTerminology
    ) {
        QuoteBundleAppConfigurationApproveButtonTerminology.APPROVE_CHANGES -> CheckoutLabel.APPROVE
        QuoteBundleAppConfigurationApproveButtonTerminology.CONFIRM_PURCHASE -> CheckoutLabel.CONFIRM
        QuoteBundleAppConfigurationApproveButtonTerminology.UNKNOWN__ -> CheckoutLabel.UNKNOWN
    }
    SignMethod.NORWEGIAN_BANK_ID, // Deprecated
    SignMethod.DANISH_BANK_ID, // Deprecated
    SignMethod.UNKNOWN__ -> CheckoutLabel.UNKNOWN
}

fun QuoteCartSubscription.QuoteCart.checkoutLabel() = when {
    checkoutMethods.contains(CheckoutMethod.SWEDISH_BANK_ID) -> CheckoutLabel.SIGN_UP
    checkoutMethods.contains(CheckoutMethod.APPROVE_ONLY) -> CheckoutLabel.APPROVE
    checkoutMethods.contains(CheckoutMethod.SIMPLE_SIGN) -> CheckoutLabel.CONTINUE
    checkoutMethods.contains(CheckoutMethod.APPROVE_ONLY) -> when (
        bundle?.fragments?.quoteBundleFragment?.appConfiguration?.approveButtonTerminology
    ) {
        QuoteBundleAppConfigurationApproveButtonTerminology.APPROVE_CHANGES -> CheckoutLabel.APPROVE
        QuoteBundleAppConfigurationApproveButtonTerminology.CONFIRM_PURCHASE -> CheckoutLabel.CONFIRM
        QuoteBundleAppConfigurationApproveButtonTerminology.UNKNOWN__ -> CheckoutLabel.UNKNOWN
        null -> CheckoutLabel.UNKNOWN
    }
    else -> CheckoutLabel.UNKNOWN
}
