package com.hedvig.android.shared.foreverui.ui.data

import com.hedvig.android.core.uidata.UiMoney
import octopus.FullReferralsQuery
import octopus.type.MemberReferralStatus

data class ForeverData(
  val campaignCode: String?,
  val incentive: UiMoney?,
  val currentNetCost: UiMoney?,
  val currentGrossCost: UiMoney?,
  val currentDiscount: UiMoney?,
  val referrals: List<Referral>,
  val referredBy: ReferredByInfo?,
) {
  constructor(
    referralsData: FullReferralsQuery.Data,
  ) : this(
    campaignCode = referralsData.currentMember.referralInformation.code,
    incentive = UiMoney.fromMoneyFragment(referralsData.currentMember.referralInformation.monthlyDiscountPerReferral),
    currentDiscount = UiMoney.fromMoneyFragment(referralsData.currentMember.insuranceCost.monthlyDiscount),
    currentNetCost = UiMoney.fromMoneyFragment(referralsData.currentMember.insuranceCost.monthlyNet),
    currentGrossCost = UiMoney.fromMoneyFragment(referralsData.currentMember.insuranceCost.monthlyGross),
    referrals = referralsData.currentMember.referralInformation.referrals.map { referral ->
      Referral(
        name = referral.name,
        state = referral.status.toState(),
        discount = referral.activeDiscount?.let { UiMoney.fromMoneyFragment(it) },
      )
    },
    referredBy = with(referralsData.currentMember.referralInformation.referredBy) {
      if (this != null && status == MemberReferralStatus.ACTIVE) {
        ReferredByInfo(
          name = name,
          activeDiscount = activeDiscount?.let { UiMoney.fromMoneyFragment(it) },
          status.toState(),
        )
      } else {
        null
      }
    },
  )
}

private fun MemberReferralStatus.toState(): ReferralState {
  return when (this) {
    MemberReferralStatus.PENDING -> ReferralState.IN_PROGRESS
    MemberReferralStatus.ACTIVE -> ReferralState.ACTIVE
    MemberReferralStatus.TERMINATED -> ReferralState.TERMINATED
    MemberReferralStatus.UNKNOWN__ -> ReferralState.UNKNOWN
  }
}

data class Referral(
  val name: String?,
  val state: ReferralState,
  val discount: UiMoney?,
)

enum class ReferralState {
  ACTIVE,
  IN_PROGRESS,
  TERMINATED,
  UNKNOWN,
}

data class ReferredByInfo(
  val name: String,
  val activeDiscount: UiMoney?,
  val state: ReferralState,
)
