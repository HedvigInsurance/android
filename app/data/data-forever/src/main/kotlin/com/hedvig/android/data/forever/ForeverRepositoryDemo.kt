package com.hedvig.android.data.forever

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import giraffe.RedeemReferralCodeMutation
import octopus.ReferralsQuery
import octopus.type.CurrencyCode
import octopus.type.MemberReferralStatus

internal class ForeverRepositoryDemo : ForeverRepository {
  private var code: String = "code"

  override suspend fun getReferralsData(): Either<ErrorMessage, ReferralsQuery.Data> = either {
    ReferralsQuery.Data(
      currentMember = ReferralsQuery.Data.CurrentMember(
        insuranceCost = ReferralsQuery.Data.CurrentMember.InsuranceCost(
          monthlyGross = ReferralsQuery.Data.CurrentMember.InsuranceCost.MonthlyGross(
            __typename = "",
            amount = 220.0,
            currencyCode = CurrencyCode.SEK,
          ),
          ReferralsQuery.Data.CurrentMember.InsuranceCost.MonthlyNet(
            __typename = "",
            amount = 150.0,
            currencyCode = CurrencyCode.SEK,
          ),
          ReferralsQuery.Data.CurrentMember.InsuranceCost.MonthlyDiscount(
            __typename = "",
            amount = 70.0,
            currencyCode = CurrencyCode.SEK,
          ),
        ),
        referralInformation = ReferralsQuery.Data.CurrentMember.ReferralInformation(
          code = "DEMOCODE",
          monthlyDiscountExcludingReferrals = ReferralsQuery.Data.CurrentMember.ReferralInformation.MonthlyDiscountExcludingReferrals(
            __typename = "",
            amount = 120.0,
            currencyCode = CurrencyCode.SEK,
          ),
          monthlyDiscountPerReferral = ReferralsQuery.Data.CurrentMember.ReferralInformation.MonthlyDiscountPerReferral(
            __typename = "",
            amount = 10.0,
            currencyCode = CurrencyCode.SEK,
          ),
          referrals = listOf(
            ReferralsQuery.Data.CurrentMember.ReferralInformation.Referral(
              name = "Stylianos",
              status = MemberReferralStatus.ACTIVE,
              activeDiscount = ReferralsQuery.Data.CurrentMember.ReferralInformation.Referral.ActiveDiscount(
                __typename = "",
                amount = 10.0,
                currencyCode = CurrencyCode.SEK,
              ),
            ),
            ReferralsQuery.Data.CurrentMember.ReferralInformation.Referral(
              name = "Sladan",
              status = MemberReferralStatus.PENDING,
              activeDiscount = ReferralsQuery.Data.CurrentMember.ReferralInformation.Referral.ActiveDiscount(
                __typename = "",
                amount = 10.0,
                currencyCode = CurrencyCode.SEK,
              ),
            ),
            ReferralsQuery.Data.CurrentMember.ReferralInformation.Referral(
              name = "Julia",
              status = MemberReferralStatus.ACTIVE,
              activeDiscount = ReferralsQuery.Data.CurrentMember.ReferralInformation.Referral.ActiveDiscount(
                __typename = "",
                amount = 10.0,
                currencyCode = CurrencyCode.SEK,
              ),
            ),
          ),
        ),
      ),
    )
  }

  override suspend fun updateCode(newCode: String): Either<ForeverRepository.ReferralError, String> {
    code = newCode
    return newCode.right()
  }

  override suspend fun redeemReferralCode(
    campaignCode: CampaignCode,
  ): Either<ErrorMessage, RedeemReferralCodeMutation.Data?> = either { null }
}
