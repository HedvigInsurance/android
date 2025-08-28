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
) {
  constructor(
    referralsData: FullReferralsQuery.Data,
  ) : this(
    campaignCode = referralsData.currentMember.referralInformation.code,
    incentive = UiMoney.fromMoneyFragment(referralsData.currentMember.referralInformation.monthlyDiscountPerReferral),
    currentDiscount = UiMoney.fromMoneyFragment(referralsData.currentMember.insuranceCost.monthlyDiscount),
    currentNetCost = UiMoney.fromMoneyFragment(referralsData.currentMember.insuranceCost.monthlyNet),
    currentGrossCost = UiMoney.fromMoneyFragment(referralsData.currentMember.insuranceCost.monthlyGross),
    referrals = referralsData.currentMember.referralInformation.referrals.map {
      Referral(
        name = it.name,
        state = when (it.status) {
          MemberReferralStatus.PENDING -> ReferralState.IN_PROGRESS
          MemberReferralStatus.ACTIVE -> ReferralState.ACTIVE
          MemberReferralStatus.TERMINATED -> ReferralState.TERMINATED
          MemberReferralStatus.UNKNOWN__ -> ReferralState.UNKNOWN
        },
        discount = it.activeDiscount?.let { UiMoney.fromMoneyFragment(it) },
      )
    },
  )
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
