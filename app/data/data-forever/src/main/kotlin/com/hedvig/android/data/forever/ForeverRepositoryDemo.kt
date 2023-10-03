package com.hedvig.android.data.forever

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import giraffe.RedeemReferralCodeMutation
import giraffe.ReferralsQuery
import giraffe.fragment.CostFragment
import giraffe.fragment.MonetaryAmountFragment
import giraffe.fragment.ReferralFragment

internal class ForeverRepositoryDemo : ForeverRepository {
  private var code: String = "code"

  override suspend fun getReferralsData(): Either<ErrorMessage, ReferralsQuery.Data> = either {
    ReferralsQuery.Data(
      ReferralsQuery.ReferralInformation(
        ReferralsQuery.Campaign(
          code,
          ReferralsQuery.Incentive(
            "",
            ReferralsQuery.AsMonthlyCostDeduction(
              "",
              ReferralsQuery.Amount(
                "",
                ReferralsQuery.Amount.Fragments(
                  MonetaryAmountFragment("10", "SEK"),
                ),
              ),
            ),
          ),
        ),
        costReducedIndefiniteDiscount = ReferralsQuery.CostReducedIndefiniteDiscount(
          "",
          ReferralsQuery.CostReducedIndefiniteDiscount.Fragments(
            CostFragment(
              CostFragment.MonthlyDiscount(
                "",
                CostFragment.MonthlyDiscount.Fragments(MonetaryAmountFragment("20", "SEK")),
              ),
              CostFragment.MonthlyNet(
                "",
                CostFragment.MonthlyNet.Fragments(MonetaryAmountFragment("40", "SEK")),
              ),
              CostFragment.MonthlyGross(
                "",
                CostFragment.MonthlyGross.Fragments(MonetaryAmountFragment("60", "SEK")),
              ),
            ),
          ),
        ),
        referredBy = ReferralsQuery.ReferredBy(
          "",
          ReferralsQuery.ReferredBy.Fragments(
            ReferralFragment(
              "",
              ReferralFragment.AsActiveReferral(
                "",
                "Adam",
                ReferralFragment.Discount(
                  "",
                  ReferralFragment.Discount.Fragments(
                    MonetaryAmountFragment(
                      "10",
                      "SEK",
                    ),
                  ),
                ),
              ),
              null,
              null,
            ),
          ),
        ),
        invitations = List(2) { index ->
          ReferralsQuery.Invitation(
            "",
            ReferralsQuery.Invitation.Fragments(
              ReferralFragment(
                "",
                ReferralFragment.AsActiveReferral(
                  "",
                  if (index % 2 == 0) "Adam" else "Claire",
                  ReferralFragment.Discount(
                    "",
                    ReferralFragment.Discount.Fragments(
                      MonetaryAmountFragment(
                        "10",
                        "SEK",
                      ),
                    ),
                  ),
                ),
                null,
                null,
              ),
            ),
          )
        },
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
