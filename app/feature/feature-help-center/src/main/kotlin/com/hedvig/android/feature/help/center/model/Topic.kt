package com.hedvig.android.feature.help.center.model

internal enum class Topic(val topicId: String, linkedQuestionIds: List<String>) {
  Payments(
    "payments",
    listOf(
      Question.IsItSafeToEnterMyCreditCardInformation.questionId,
      Question.AreThereFeesWhenPayingWithCard.questionId,
      Question.CanIGetRefundForInAppPurchase.questionId,
      Question.HowToChangePaymentMethod.questionId,
      Question.WhatHappensWhenPaymentIsDeclined.questionId,
      Question.HowToCancelInsurance.questionId,
    ),
  ),
  Claims("claims", emptyList()),
  MyInsurance("myInsurance", emptyList()),
  CoInsured("coInsured", emptyList()),
  FirstVet("firstVet", emptyList()),
  Campaigns("campaigns", emptyList()),
}

internal val commonTopicIds = listOf(
  Topic.Payments.topicId,
  Topic.Claims.topicId,
  Topic.MyInsurance.topicId,
  Topic.CoInsured.topicId,
  Topic.FirstVet.topicId,
  Topic.Campaigns.topicId,
)
