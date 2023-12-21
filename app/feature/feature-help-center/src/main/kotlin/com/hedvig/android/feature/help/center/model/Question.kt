package com.hedvig.android.feature.help.center.model

enum class Question(val questionId: String) {
  WhenIsInsuranceCharged("whenIsInsuranceCharged"),
  WhenIsInsuranceActivated("whenIsInsuranceActivated"),
  HowToMakeClaim("howToMakeClaim"),
  HowViewPaymentHistory("howViewPaymentHistory"),
  WhatShouldDoWhenPaymentFails("whatShouldDoWhenPaymentFails"),

  IsItSafeToEnterMyCreditCardInformation("isItSafeToEnterMyCreditCardInformation"),
  AreThereFeesWhenPayingWithCard("areThereFeesWhenPayingWithCard"),
  CanIGetRefundForInAppPurchase("canIGetRefundForInAppPurchase"),
  HowToChangePaymentMethod("howToChangePaymentMethod"),
  WhatHappensWhenPaymentIsDeclined("whatHappensWhenPaymentIsDeclined"),
  HowToCancelInsurance("howToCancelInsurance"),
}

internal val commonQuestionIds = listOf(
  Question.WhenIsInsuranceCharged.questionId,
  Question.WhenIsInsuranceActivated.questionId,
  Question.HowToMakeClaim.questionId,
  Question.HowViewPaymentHistory.questionId,
  Question.WhatShouldDoWhenPaymentFails.questionId,
)
