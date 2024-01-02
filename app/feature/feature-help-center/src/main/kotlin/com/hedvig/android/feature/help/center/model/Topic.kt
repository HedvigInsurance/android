package com.hedvig.android.feature.help.center.model

import androidx.annotation.StringRes
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf

internal enum class Topic(
  val topicId: String,
  @StringRes val titleRes: Int,
  val commonQuestionIds: List<String>,
  val allQuestionIds: List<String>,
) {
  Payments(
    "payments",
    // todo help-center: correct string resource
    R.string.PROFILE_PAYMENT_TITLE,
    listOf(
      Question.WhenIsInsuranceCharged.questionId,
      Question.WhenIsInsuranceActivated.questionId,
    ),
    listOf(
      Question.IsItSafeToEnterMyCreditCardInformation.questionId,
      Question.AreThereFeesWhenPayingWithCard.questionId,
      Question.CanIGetRefundForInAppPurchase.questionId,
      Question.HowToChangePaymentMethod.questionId,
      Question.WhatHappensWhenPaymentIsDeclined.questionId,
      Question.HowToCancelInsurance.questionId,
    ),
  ),
  Claims("claims", R.string.CLAIMS_CASE, emptyList(), emptyList()),
  MyInsurance("myInsurance", R.string.CONTRACT_CHANGE_INFORMATION_TITLE, emptyList(), emptyList()),
  CoInsured("coInsured", R.string.COINSURED_EDIT_TITLE, emptyList(), emptyList()),
  FirstVet("firstVet", R.string.CROSS_SELL_PET_TITLE, emptyList(), emptyList()),
  Campaigns("campaigns", R.string.PROFILE_PAYMENT_TITLE, emptyList(), emptyList()),
}

internal val commonTopics = persistentListOf(
  Topic.Payments,
  Topic.Claims,
  Topic.MyInsurance,
  Topic.CoInsured,
  Topic.FirstVet,
  Topic.Campaigns,
)
