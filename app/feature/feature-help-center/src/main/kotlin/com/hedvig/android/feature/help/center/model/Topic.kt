package com.hedvig.android.feature.help.center.model

import androidx.annotation.StringRes
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf

internal enum class Topic(
  val topicId: String,
  @StringRes val titleRes: Int,
  val linkedQuestionIds: List<String>,
) {
  Payments(
    "payments",
    // todo help-center: correct string resource
    R.string.PROFILE_PAYMENT_TITLE,
    listOf(
      Question.IsItSafeToEnterMyCreditCardInformation.questionId,
      Question.AreThereFeesWhenPayingWithCard.questionId,
      Question.CanIGetRefundForInAppPurchase.questionId,
      Question.HowToChangePaymentMethod.questionId,
      Question.WhatHappensWhenPaymentIsDeclined.questionId,
      Question.HowToCancelInsurance.questionId,
    ),
  ),
  Claims("claims", R.string.PROFILE_PAYMENT_TITLE, emptyList()),
  MyInsurance("myInsurance", R.string.PROFILE_PAYMENT_TITLE, emptyList()),
  CoInsured("coInsured", R.string.PROFILE_PAYMENT_TITLE, emptyList()),
  FirstVet("firstVet", R.string.PROFILE_PAYMENT_TITLE, emptyList()),
  Campaigns("campaigns", R.string.PROFILE_PAYMENT_TITLE, emptyList()),
}

internal val commonTopics = persistentListOf(
  Topic.Payments,
  Topic.Claims,
  Topic.MyInsurance,
  Topic.CoInsured,
  Topic.FirstVet,
  Topic.Campaigns,
)
