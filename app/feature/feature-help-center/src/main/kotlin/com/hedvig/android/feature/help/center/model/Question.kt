package com.hedvig.android.feature.help.center.model

import androidx.annotation.StringRes
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf

enum class Question(
  val questionId: String,
  @StringRes val questionRes: Int,
) {
  // todo help-center: correct string resource
  WhenIsInsuranceCharged("whenIsInsuranceCharged", R.string.intro_more_info_whats_insured_people_button),
  WhenIsInsuranceActivated("whenIsInsuranceActivated", R.string.intro_more_info_whats_insured_people_button),
  HowToMakeClaim("howToMakeClaim", R.string.intro_more_info_whats_insured_people_button),
  HowViewPaymentHistory("howViewPaymentHistory", R.string.intro_more_info_whats_insured_people_button),
  WhatShouldDoWhenPaymentFails("whatShouldDoWhenPaymentFails", R.string.intro_more_info_whats_insured_people_button),

  IsItSafeToEnterMyCreditCardInformation(
    "isItSafeToEnterMyCreditCardInformation",
    R.string.intro_more_info_whats_insured_people_button,
  ),
  AreThereFeesWhenPayingWithCard(
    "areThereFeesWhenPayingWithCard",
    R.string.intro_more_info_whats_insured_people_button,
  ),
  CanIGetRefundForInAppPurchase("canIGetRefundForInAppPurchase", R.string.intro_more_info_whats_insured_people_button),
  HowToChangePaymentMethod("howToChangePaymentMethod", R.string.intro_more_info_whats_insured_people_button),
  WhatHappensWhenPaymentIsDeclined(
    "whatHappensWhenPaymentIsDeclined",
    R.string.intro_more_info_whats_insured_people_button,
  ),
  HowToCancelInsurance("howToCancelInsurance", R.string.intro_more_info_whats_insured_people_button),
}

internal val commonQuestions = persistentListOf(
  Question.WhenIsInsuranceCharged,
  Question.WhenIsInsuranceActivated,
  Question.HowToMakeClaim,
  Question.HowViewPaymentHistory,
  Question.WhatShouldDoWhenPaymentFails,
)
