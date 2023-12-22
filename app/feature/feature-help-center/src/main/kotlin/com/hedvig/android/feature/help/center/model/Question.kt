package com.hedvig.android.feature.help.center.model

import androidx.annotation.StringRes
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf

enum class Question(
  val questionId: String,
  val relatedQuestionIds: List<String>,
  @StringRes val titleRes: Int,
  @StringRes val questionRes: Int,
  @StringRes val answerRes: Int,
) {
  // todo help-center: correct string resource for all Res entries
  WhenIsInsuranceCharged(
    WhenIsInsuranceChargedId,
    persistentListOf(
      WhenIsInsuranceChargedId,
      WhenIsInsuranceActivatedId,
      HowToMakeClaimId,
    ),
    R.string.PROFILE_PAYMENT_TITLE,
    R.string.intro_more_info_whats_insured_people_button,
    R.string.intro_body_edit_insured_people,
  ),
  WhenIsInsuranceActivated(
    WhenIsInsuranceActivatedId,
    persistentListOf(),
    R.string.PROFILE_PAYMENT_TITLE,
    R.string.intro_more_info_whats_insured_people_button,
    R.string.intro_body_edit_insured_people,
  ),
  HowToMakeClaim(
    HowToMakeClaimId,
    persistentListOf(),
    R.string.PROFILE_PAYMENT_TITLE,
    R.string.intro_more_info_whats_insured_people_button,
    R.string.intro_body_edit_insured_people,
  ),
  HowViewPaymentHistory(
    HowViewPaymentHistoryId,
    persistentListOf(),
    R.string.PROFILE_PAYMENT_TITLE,
    R.string.intro_more_info_whats_insured_people_button,
    R.string.intro_body_edit_insured_people,
  ),
  WhatShouldDoWhenPaymentFails(
    WhatShouldDoWhenPaymentFailsId,
    persistentListOf(),
    R.string.PROFILE_PAYMENT_TITLE,
    R.string.intro_more_info_whats_insured_people_button,
    R.string.intro_body_edit_insured_people,
  ),

  IsItSafeToEnterMyCreditCardInformation(
    IsItSafeToEnterMyCreditCardInformationId,
    persistentListOf(),
    R.string.PROFILE_PAYMENT_TITLE,
    R.string.intro_more_info_whats_insured_people_button,
    R.string.intro_body_edit_insured_people,
  ),
  AreThereFeesWhenPayingWithCard(
    AreThereFeesWhenPayingWithCardId,
    persistentListOf(),
    R.string.PROFILE_PAYMENT_TITLE,
    R.string.intro_more_info_whats_insured_people_button,
    R.string.intro_body_edit_insured_people,
  ),
  CanIGetRefundForInAppPurchase(
    CanIGetRefundForInAppPurchaseId,
    persistentListOf(),
    R.string.PROFILE_PAYMENT_TITLE,
    R.string.intro_more_info_whats_insured_people_button,
    R.string.intro_body_edit_insured_people,
  ),
  HowToChangePaymentMethod(
    HowToChangePaymentMethodId,
    persistentListOf(),
    R.string.PROFILE_PAYMENT_TITLE,
    R.string.intro_more_info_whats_insured_people_button,
    R.string.intro_body_edit_insured_people,
  ),
  WhatHappensWhenPaymentIsDeclined(
    WhatHappensWhenPaymentIsDeclinedId,
    persistentListOf(),
    R.string.PROFILE_PAYMENT_TITLE,
    R.string.intro_more_info_whats_insured_people_button,
    R.string.intro_body_edit_insured_people,
  ),
  HowToCancelInsurance(
    HowToCancelInsuranceId,
    persistentListOf(),
    R.string.PROFILE_PAYMENT_TITLE,
    R.string.intro_more_info_whats_insured_people_button,
    R.string.intro_body_edit_insured_people,
  ),
}

const val WhenIsInsuranceChargedId = "whenIsInsuranceCharged"
const val WhenIsInsuranceActivatedId = "whenIsInsuranceActivated"
const val HowToMakeClaimId = "howToMakeClaim"
const val HowViewPaymentHistoryId = "howViewPaymentHistory"
const val WhatShouldDoWhenPaymentFailsId = "whatShouldDoWhenPaymentFails"
const val IsItSafeToEnterMyCreditCardInformationId = "isItSafeToEnterMyCreditCardInformation"
const val AreThereFeesWhenPayingWithCardId = "areThereFeesWhenPayingWithCard"
const val CanIGetRefundForInAppPurchaseId = "canIGetRefundForInAppPurchase"
const val HowToChangePaymentMethodId = "howToChangePaymentMethod"
const val WhatHappensWhenPaymentIsDeclinedId = "whatHappensWhenPaymentIsDeclined"
const val HowToCancelInsuranceId = "howToCancelInsurance"

internal val commonQuestions = persistentListOf(
  Question.WhenIsInsuranceCharged,
  Question.WhenIsInsuranceActivated,
  Question.HowToMakeClaim,
  Question.HowViewPaymentHistory,
  Question.WhatShouldDoWhenPaymentFails,
)
