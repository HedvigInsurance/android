package com.hedvig.android.data.contract

import com.hedvig.android.data.contract.ContractType.DK_ACCIDENT
import com.hedvig.android.data.contract.ContractType.DK_ACCIDENT_STUDENT
import com.hedvig.android.data.contract.ContractType.DK_HOME_CONTENT_OWN
import com.hedvig.android.data.contract.ContractType.DK_HOME_CONTENT_RENT
import com.hedvig.android.data.contract.ContractType.DK_HOME_CONTENT_STUDENT_OWN
import com.hedvig.android.data.contract.ContractType.DK_HOME_CONTENT_STUDENT_RENT
import com.hedvig.android.data.contract.ContractType.DK_HOUSE
import com.hedvig.android.data.contract.ContractType.DK_TRAVEL
import com.hedvig.android.data.contract.ContractType.DK_TRAVEL_STUDENT
import com.hedvig.android.data.contract.ContractType.NO_ACCIDENT
import com.hedvig.android.data.contract.ContractType.NO_HOME_CONTENT_OWN
import com.hedvig.android.data.contract.ContractType.NO_HOME_CONTENT_RENT
import com.hedvig.android.data.contract.ContractType.NO_HOME_CONTENT_STUDENT_OWN
import com.hedvig.android.data.contract.ContractType.NO_HOME_CONTENT_STUDENT_RENT
import com.hedvig.android.data.contract.ContractType.NO_HOME_CONTENT_YOUTH_OWN
import com.hedvig.android.data.contract.ContractType.NO_HOME_CONTENT_YOUTH_RENT
import com.hedvig.android.data.contract.ContractType.NO_HOUSE
import com.hedvig.android.data.contract.ContractType.NO_TRAVEL
import com.hedvig.android.data.contract.ContractType.NO_TRAVEL_STUDENT
import com.hedvig.android.data.contract.ContractType.NO_TRAVEL_YOUTH
import com.hedvig.android.data.contract.ContractType.SE_ACCIDENT
import com.hedvig.android.data.contract.ContractType.SE_ACCIDENT_STUDENT
import com.hedvig.android.data.contract.ContractType.SE_APARTMENT_BRF
import com.hedvig.android.data.contract.ContractType.SE_APARTMENT_RENT
import com.hedvig.android.data.contract.ContractType.SE_APARTMENT_STUDENT_BRF
import com.hedvig.android.data.contract.ContractType.SE_APARTMENT_STUDENT_RENT
import com.hedvig.android.data.contract.ContractType.SE_CAR_FULL
import com.hedvig.android.data.contract.ContractType.SE_CAR_HALF
import com.hedvig.android.data.contract.ContractType.SE_CAR_TRAFFIC
import com.hedvig.android.data.contract.ContractType.SE_CAR_TRIAL_FULL
import com.hedvig.android.data.contract.ContractType.SE_CAR_TRIAL_HALF
import com.hedvig.android.data.contract.ContractType.SE_CAT_BASIC
import com.hedvig.android.data.contract.ContractType.SE_CAT_PREMIUM
import com.hedvig.android.data.contract.ContractType.SE_CAT_STANDARD
import com.hedvig.android.data.contract.ContractType.SE_DOG_BASIC
import com.hedvig.android.data.contract.ContractType.SE_DOG_PREMIUM
import com.hedvig.android.data.contract.ContractType.SE_DOG_STANDARD
import com.hedvig.android.data.contract.ContractType.SE_GROUP_APARTMENT_BRF
import com.hedvig.android.data.contract.ContractType.SE_GROUP_APARTMENT_RENT
import com.hedvig.android.data.contract.ContractType.SE_HOUSE
import com.hedvig.android.data.contract.ContractType.SE_QASA_LONG_TERM_RENTAL
import com.hedvig.android.data.contract.ContractType.SE_QASA_SHORT_TERM_RENTAL
import com.hedvig.android.data.contract.ContractType.UNKNOWN

enum class ContractType {
  NO_HOUSE,
  DK_HOUSE,
  SE_HOUSE,
  DK_ACCIDENT,
  NO_ACCIDENT,
  SE_ACCIDENT,
  SE_ACCIDENT_STUDENT,
  DK_ACCIDENT_STUDENT,
  DK_TRAVEL,
  DK_TRAVEL_STUDENT,
  NO_TRAVEL,
  NO_TRAVEL_STUDENT,
  NO_TRAVEL_YOUTH,
  DK_HOME_CONTENT_RENT,
  DK_HOME_CONTENT_STUDENT_RENT,
  NO_HOME_CONTENT_RENT,
  SE_APARTMENT_RENT,
  SE_GROUP_APARTMENT_RENT,
  SE_QASA_LONG_TERM_RENTAL,
  SE_QASA_SHORT_TERM_RENTAL,
  NO_HOME_CONTENT_YOUTH_RENT,
  NO_HOME_CONTENT_YOUTH_OWN,
  DK_HOME_CONTENT_STUDENT_OWN,
  DK_HOME_CONTENT_OWN,
  NO_HOME_CONTENT_OWN,
  SE_APARTMENT_BRF,
  SE_GROUP_APARTMENT_BRF,
  NO_HOME_CONTENT_STUDENT_OWN,
  NO_HOME_CONTENT_STUDENT_RENT,
  SE_APARTMENT_STUDENT_BRF,
  SE_APARTMENT_STUDENT_RENT,
  SE_CAR_FULL,
  SE_CAR_HALF,
  SE_CAR_TRAFFIC,
  SE_CAR_TRIAL_HALF,
  SE_CAR_TRIAL_FULL,
  SE_CAT_BASIC,
  SE_CAT_PREMIUM,
  SE_CAT_STANDARD,
  SE_DOG_BASIC,
  SE_DOG_PREMIUM,
  SE_DOG_STANDARD,
  UNKNOWN,
}

fun ContractType.isTrialContract() = when (this) {
  NO_HOUSE,
  DK_HOUSE,
  SE_HOUSE,
  DK_ACCIDENT,
  NO_ACCIDENT,
  SE_ACCIDENT,
  SE_ACCIDENT_STUDENT,
  DK_ACCIDENT_STUDENT,
  DK_TRAVEL,
  DK_TRAVEL_STUDENT,
  NO_TRAVEL,
  NO_TRAVEL_STUDENT,
  NO_TRAVEL_YOUTH,
  DK_HOME_CONTENT_RENT,
  DK_HOME_CONTENT_STUDENT_RENT,
  NO_HOME_CONTENT_RENT,
  SE_APARTMENT_RENT,
  SE_QASA_LONG_TERM_RENTAL,
  SE_QASA_SHORT_TERM_RENTAL,
  NO_HOME_CONTENT_YOUTH_RENT,
  NO_HOME_CONTENT_YOUTH_OWN,
  DK_HOME_CONTENT_STUDENT_OWN,
  DK_HOME_CONTENT_OWN,
  NO_HOME_CONTENT_OWN,
  SE_APARTMENT_BRF,
  NO_HOME_CONTENT_STUDENT_OWN,
  NO_HOME_CONTENT_STUDENT_RENT,
  SE_APARTMENT_STUDENT_BRF,
  SE_APARTMENT_STUDENT_RENT,
  SE_CAR_FULL,
  SE_CAR_HALF,
  SE_CAR_TRAFFIC,
  SE_CAT_BASIC,
  SE_CAT_PREMIUM,
  SE_CAT_STANDARD,
  SE_DOG_BASIC,
  SE_DOG_PREMIUM,
  SE_DOG_STANDARD,
  UNKNOWN,
  -> false

  SE_CAR_TRIAL_FULL,
  SE_CAR_TRIAL_HALF,
  SE_GROUP_APARTMENT_RENT,
  SE_GROUP_APARTMENT_BRF,
  -> true
}

fun ContractType.supportsTravelCertificate(): Boolean = when (this) {
  SE_HOUSE,
  SE_APARTMENT_BRF,
  SE_APARTMENT_STUDENT_BRF,
  SE_APARTMENT_STUDENT_RENT,
  -> true

  else -> false
}

fun String.toContractType(): ContractType = when (this) {
  "NO_HOUSE" -> NO_HOUSE
  "DK_HOUSE" -> DK_HOUSE
  "SE_HOUSE" -> SE_HOUSE
  "DK_ACCIDENT" -> DK_ACCIDENT
  "NO_ACCIDENT" -> NO_ACCIDENT
  "SE_ACCIDENT" -> SE_ACCIDENT
  "SE_ACCIDENT_STUDENT" -> SE_ACCIDENT_STUDENT
  "DK_ACCIDENT_STUDENT" -> DK_ACCIDENT_STUDENT
  "DK_TRAVEL" -> DK_TRAVEL
  "DK_TRAVEL_STUDENT" -> DK_TRAVEL_STUDENT
  "NO_TRAVEL" -> NO_TRAVEL
  "NO_TRAVEL_STUDENT" -> NO_TRAVEL_STUDENT
  "NO_TRAVEL_YOUTH" -> NO_TRAVEL_YOUTH
  "DK_HOME_CONTENT_RENT" -> DK_HOME_CONTENT_RENT
  "DK_HOME_CONTENT_STUDENT_RENT" -> DK_HOME_CONTENT_STUDENT_RENT
  "NO_HOME_CONTENT_RENT" -> NO_HOME_CONTENT_RENT
  "SE_APARTMENT_RENT" -> SE_APARTMENT_RENT
  "SE_GROUP_APARTMENT_RENT" -> SE_GROUP_APARTMENT_RENT
  "SE_QASA_LONG_TERM_RENTAL" -> SE_QASA_LONG_TERM_RENTAL
  "SE_QASA_SHORT_TERM_RENTAL" -> SE_QASA_SHORT_TERM_RENTAL
  "NO_HOME_CONTENT_YOUTH_RENT" -> NO_HOME_CONTENT_YOUTH_RENT
  "NO_HOME_CONTENT_YOUTH_OWN" -> NO_HOME_CONTENT_YOUTH_OWN
  "DK_HOME_CONTENT_STUDENT_OWN" -> DK_HOME_CONTENT_STUDENT_OWN
  "DK_HOME_CONTENT_OWN" -> DK_HOME_CONTENT_OWN
  "NO_HOME_CONTENT_OWN" -> NO_HOME_CONTENT_OWN
  "SE_APARTMENT_BRF" -> SE_APARTMENT_BRF
  "SE_GROUP_APARTMENT_BRF" -> SE_GROUP_APARTMENT_BRF
  "NO_HOME_CONTENT_STUDENT_OWN" -> NO_HOME_CONTENT_STUDENT_OWN
  "NO_HOME_CONTENT_STUDENT_RENT" -> NO_HOME_CONTENT_STUDENT_RENT
  "SE_APARTMENT_STUDENT_BRF" -> SE_APARTMENT_STUDENT_BRF
  "SE_APARTMENT_STUDENT_RENT" -> SE_APARTMENT_STUDENT_RENT
  "SE_CAR_FULL" -> SE_CAR_FULL
  "SE_CAR_HALF" -> SE_CAR_HALF
  "SE_CAR_TRAFFIC" -> SE_CAR_TRAFFIC
  "SE_CAR_TRIAL_HALF" -> SE_CAR_TRIAL_HALF
  "SE_CAR_TRIAL_FULL" -> SE_CAR_TRIAL_FULL
  "SE_CAT_BASIC" -> SE_CAT_BASIC
  "SE_CAT_PREMIUM" -> SE_CAT_PREMIUM
  "SE_CAT_STANDARD" -> SE_CAT_STANDARD
  "SE_DOG_BASIC" -> SE_DOG_BASIC
  "SE_DOG_PREMIUM" -> SE_DOG_PREMIUM
  "SE_DOG_STANDARD" -> SE_DOG_STANDARD
  else -> UNKNOWN
}
