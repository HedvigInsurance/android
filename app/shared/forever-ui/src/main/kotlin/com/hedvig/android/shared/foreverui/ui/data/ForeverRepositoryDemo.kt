package com.hedvig.android.shared.foreverui.ui.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import octopus.FullReferralsQuery
import octopus.type.CurrencyCode
import octopus.type.MemberReferralStatus

internal class ForeverRepositoryDemo : ForeverRepository {
  private var code: String = "code"

  override suspend fun getReferralsData(): Either<ErrorMessage, FullReferralsQuery.Data> = either {
    @Suppress("ktlint:standard:max-line-length")
    FullReferralsQuery.Data(
      currentMember = FullReferralsQuery.Data.CurrentMember(
        __typename = "",
        id = "id",
        insuranceCost = FullReferralsQuery.Data.CurrentMember.InsuranceCost(
          monthlyGross = FullReferralsQuery.Data.CurrentMember.InsuranceCost.MonthlyGross(
            __typename = "",
            amount = 60.0,
            currencyCode = CurrencyCode.SEK,
          ),
          FullReferralsQuery.Data.CurrentMember.InsuranceCost.MonthlyNet(
            __typename = "",
            amount = 40.0,
            currencyCode = CurrencyCode.SEK,
          ),
          FullReferralsQuery.Data.CurrentMember.InsuranceCost.MonthlyDiscount(
            __typename = "",
            amount = 20.0,
            currencyCode = CurrencyCode.SEK,
          ),
        ),
        referralInformation = FullReferralsQuery.Data.CurrentMember.ReferralInformation(
          code = "DEMOCODE",
          monthlyDiscountPerReferral = FullReferralsQuery.Data.CurrentMember.ReferralInformation.MonthlyDiscountPerReferral(
            __typename = "",
            amount = 10.0,
            currencyCode = CurrencyCode.SEK,
          ),
          referredBy = null,
          referrals = listOf(
            FullReferralsQuery.Data.CurrentMember.ReferralInformation.Referral(
              name = "Adam",
              status = MemberReferralStatus.ACTIVE,
              activeDiscount = FullReferralsQuery.Data.CurrentMember.ReferralInformation.Referral.ActiveDiscount(
                __typename = "",
                amount = 10.0,
                currencyCode = CurrencyCode.SEK,
              ),
            ),
            FullReferralsQuery.Data.CurrentMember.ReferralInformation.Referral(
              name = "Claire",
              status = MemberReferralStatus.PENDING,
              activeDiscount = FullReferralsQuery.Data.CurrentMember.ReferralInformation.Referral.ActiveDiscount(
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
}
