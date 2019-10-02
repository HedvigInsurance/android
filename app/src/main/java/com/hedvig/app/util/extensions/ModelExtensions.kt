package com.hedvig.app.util.extensions

import com.hedvig.android.owldroid.graphql.ProfileQuery

fun ProfileQuery.Campaign.monthlyCostDeductionIncentive() =
    this.incentive as? ProfileQuery.AsMonthlyCostDeduction
