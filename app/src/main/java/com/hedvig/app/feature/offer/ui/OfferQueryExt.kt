package com.hedvig.app.feature.offer.ui

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationApproveButtonTerminology
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.android.owldroid.type.TypeOfContractGradientOption
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

fun OfferQuery.Data.checkoutLabel() = when (signMethodForQuotes) {
    SignMethod.SWEDISH_BANK_ID -> CheckoutLabel.SIGN_UP
    SignMethod.SIMPLE_SIGN -> CheckoutLabel.CONTINUE
    SignMethod.APPROVE_ONLY -> when (this.quoteBundle.appConfiguration.approveButtonTerminology) {
        QuoteBundleAppConfigurationApproveButtonTerminology.APPROVE_CHANGES -> CheckoutLabel.APPROVE
        QuoteBundleAppConfigurationApproveButtonTerminology.CONFIRM_PURCHASE -> CheckoutLabel.CONFIRM
        QuoteBundleAppConfigurationApproveButtonTerminology.UNKNOWN__ -> CheckoutLabel.UNKNOWN
    }
    SignMethod.NORWEGIAN_BANK_ID, // Deprecated
    SignMethod.DANISH_BANK_ID, // Deprecated
    SignMethod.UNKNOWN__ -> CheckoutLabel.UNKNOWN
}

fun OfferQuery.Data.gradientType() = when (quoteBundle.appConfiguration.gradientOption) {
    TypeOfContractGradientOption.GRADIENT_ONE -> GradientType.FALL_SUNSET
    TypeOfContractGradientOption.GRADIENT_TWO -> GradientType.SPRING_FOG
    TypeOfContractGradientOption.GRADIENT_THREE -> GradientType.SUMMER_SKY
    TypeOfContractGradientOption.UNKNOWN__ -> GradientType.SPRING_FOG
}
