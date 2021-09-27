package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import com.hedvig.android.owldroid.type.TypeOfContract

fun TypeOfContract.canChangeAddress() = when (this) {
    TypeOfContract.SE_HOUSE,
    TypeOfContract.SE_APARTMENT_BRF,
    TypeOfContract.SE_APARTMENT_RENT,
    TypeOfContract.SE_APARTMENT_STUDENT_BRF,
    TypeOfContract.SE_APARTMENT_STUDENT_RENT,
    TypeOfContract.NO_HOME_CONTENT_OWN,
    TypeOfContract.NO_HOME_CONTENT_RENT,
    TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
    TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
    TypeOfContract.NO_TRAVEL,
    TypeOfContract.NO_TRAVEL_YOUTH,
    TypeOfContract.DK_HOME_CONTENT_OWN,
    TypeOfContract.DK_HOME_CONTENT_RENT,
    TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN,
    TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT,
    TypeOfContract.DK_ACCIDENT,
    TypeOfContract.DK_ACCIDENT_STUDENT,
    TypeOfContract.DK_TRAVEL,
    TypeOfContract.DK_TRAVEL_STUDENT -> true
    TypeOfContract.SE_ACCIDENT,
    TypeOfContract.SE_ACCIDENT_STUDENT,
    TypeOfContract.UNKNOWN__ -> false
}
