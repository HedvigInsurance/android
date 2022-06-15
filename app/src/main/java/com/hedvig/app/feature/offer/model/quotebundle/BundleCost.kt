package com.hedvig.app.feature.offer.model.quotebundle

import com.hedvig.android.owldroid.graphql.fragment.QuoteBundleFragment
import com.hedvig.app.util.apollo.toMonetaryAmount
import javax.money.MonetaryAmount

data class BundleCost constructor(
    val grossMonthlyCost: MonetaryAmount,
    val netMonthlyCost: MonetaryAmount,
    val ignoreCampaigns: Boolean
) {
    val finalPremium: MonetaryAmount
        get() = if (ignoreCampaigns) {
            grossMonthlyCost
        } else {
            netMonthlyCost
        }
}

fun QuoteBundleFragment.toBundleCost() = BundleCost(
    grossMonthlyCost = bundleCost.grossMonthlyCost(),
    netMonthlyCost = bundleCost.netMonthlyCost(),
    ignoreCampaigns = appConfiguration.ignoreCampaigns
)

fun QuoteBundleFragment.BundleCost.netMonthlyCost() = fragments
    .costFragment
    .monthlyNet
    .fragments
    .monetaryAmountFragment
    .toMonetaryAmount()

fun QuoteBundleFragment.BundleCost.grossMonthlyCost() = fragments
    .costFragment
    .monthlyGross
    .fragments
    .monetaryAmountFragment
    .toMonetaryAmount()
