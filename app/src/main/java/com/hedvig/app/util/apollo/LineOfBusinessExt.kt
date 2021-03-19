package com.hedvig.app.util.apollo

import com.hedvig.android.owldroid.type.DanishHomeContentLineOfBusiness
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.app.R

fun SwedishApartmentLineOfBusiness.stringRes() = when (this) {
    SwedishApartmentLineOfBusiness.RENT -> R.string.SWEDISH_APARTMENT_LOB_RENT
    SwedishApartmentLineOfBusiness.BRF -> R.string.SWEDISH_APARTMENT_LOB_BRF
    SwedishApartmentLineOfBusiness.STUDENT_RENT -> R.string.SWEDISH_APARTMENT_LOB_STUDENT_RENT
    SwedishApartmentLineOfBusiness.STUDENT_BRF -> R.string.SWEDISH_APARTMENT_LOB_STUDENT_BRF
    SwedishApartmentLineOfBusiness.UNKNOWN__ -> null
}

fun NorwegianHomeContentLineOfBusiness.stringRes() = when (this) {
    NorwegianHomeContentLineOfBusiness.RENT -> R.string.NORWEIGIAN_HOME_CONTENT_LOB_RENT
    NorwegianHomeContentLineOfBusiness.OWN -> R.string.NORWEIGIAN_HOME_CONTENT_LOB_OWN
    NorwegianHomeContentLineOfBusiness.YOUTH_RENT -> R.string.NORWEIGIAN_HOME_CONTENT_LOB_STUDENT_RENT
    NorwegianHomeContentLineOfBusiness.YOUTH_OWN -> R.string.NORWEIGIAN_HOME_CONTENT_LOB_STUDENT_OWN
    NorwegianHomeContentLineOfBusiness.UNKNOWN__ -> null
}

fun DanishHomeContentLineOfBusiness.stringRes() = when (this) {
    DanishHomeContentLineOfBusiness.RENT -> R.string.DANISH_HOME_CONTENT_LOB_RENT
    DanishHomeContentLineOfBusiness.OWN -> R.string.DANISH_HOME_CONTENT_LOB_OWN
    DanishHomeContentLineOfBusiness.STUDENT_RENT -> R.string.DANISH_HOME_CONTENT_LOB_STUDENT_RENT
    DanishHomeContentLineOfBusiness.STUDENT_OWN -> R.string.DANISH_HOME_CONTENT_LOB_STUDENT_OWN
    DanishHomeContentLineOfBusiness.UNKNOWN__ -> null
}

