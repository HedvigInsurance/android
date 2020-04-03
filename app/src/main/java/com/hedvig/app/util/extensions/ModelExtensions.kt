package com.hedvig.app.util.extensions

import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R

fun ProfileQuery.Campaign.monthlyCostDeductionIncentive() =
    this.incentive?.asMonthlyCostDeduction

fun TypeOfContract.getStringId() = when (this) {
    TypeOfContract.SE_HOUSE -> R.string.SWEDISH_HOUSE_LOB
    TypeOfContract.SE_APARTMENT_BRF -> R.string.SWEDISH_APARTMENT_LOB_BRF
    TypeOfContract.SE_APARTMENT_RENT -> R.string.SWEDISH_APARTMENT_LOB_RENT
    TypeOfContract.SE_APARTMENT_STUDENT_BRF -> R.string.SWEDISH_APARTMENT_LOB_STUDENT_BRF
    TypeOfContract.SE_APARTMENT_STUDENT_RENT -> R.string.SWEDISH_APARTMENT_LOB_STUDENT_RENT
    TypeOfContract.NO_HOME_CONTENT_OWN -> R.string.NORWEIGIAN_HOME_CONTENT_LOB_OWN
    TypeOfContract.NO_HOME_CONTENT_RENT -> R.string.NORWEIGIAN_HOME_CONTENT_LOB_RENT
    TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN -> R.string.NORWEIGIAN_HOME_CONTENT_LOB_STUDENT_OWN
    TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT -> R.string.NORWEIGIAN_HOME_CONTENT_LOB_STUDENT_OWN
    TypeOfContract.NO_TRAVEL -> TODO()
    TypeOfContract.NO_TRAVEL_YOUTH -> TODO()
    TypeOfContract.UNKNOWN__ -> TODO()
}
