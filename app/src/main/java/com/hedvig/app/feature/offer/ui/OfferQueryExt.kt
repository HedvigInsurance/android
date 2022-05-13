package com.hedvig.app.feature.offer.ui

import com.hedvig.android.owldroid.fragment.QuoteBundleFragment
import com.hedvig.android.owldroid.type.CheckoutMethod
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationApproveButtonTerminology
import com.hedvig.app.feature.offer.model.CheckoutLabel

fun QuoteBundleFragment.checkoutLabel(checkoutMethods: List<CheckoutMethod>) = when {
    checkoutMethods.contains(CheckoutMethod.SWEDISH_BANK_ID) -> CheckoutLabel.SIGN_UP
    checkoutMethods.contains(CheckoutMethod.APPROVE_ONLY) -> CheckoutLabel.APPROVE
    checkoutMethods.contains(CheckoutMethod.SIMPLE_SIGN) -> CheckoutLabel.CONTINUE
    checkoutMethods.contains(CheckoutMethod.APPROVE_ONLY) -> when (
        appConfiguration.approveButtonTerminology
    ) {
        QuoteBundleAppConfigurationApproveButtonTerminology.APPROVE_CHANGES -> CheckoutLabel.APPROVE
        QuoteBundleAppConfigurationApproveButtonTerminology.CONFIRM_PURCHASE -> CheckoutLabel.CONFIRM
        QuoteBundleAppConfigurationApproveButtonTerminology.UNKNOWN__ -> CheckoutLabel.UNKNOWN
    }
    else -> CheckoutLabel.UNKNOWN
}
