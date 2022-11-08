package com.hedvig.app.feature.referrals.ui.tab

import com.hedvig.android.apollo.graphql.ReferralsQuery
import com.hedvig.android.apollo.graphql.fragment.ReferralFragment

sealed class ReferralsModel {

  object Title : ReferralsModel()

  sealed class Header : ReferralsModel() {
    object LoadingHeader : Header()
    data class LoadedEmptyHeader(
      val inner: ReferralsQuery.Data,
    ) : Header()

    data class LoadedHeader(
      val inner: ReferralsQuery.Data,
    ) : Header()
  }

  sealed class Code : ReferralsModel() {
    object LoadingCode : Code()
    data class LoadedCode(
      val inner: ReferralsQuery.Data,
    ) : Code()
  }

  object InvitesHeader : ReferralsModel()

  sealed class Referral : ReferralsModel() {
    object LoadingReferral : Referral()

    data class Referee(
      val inner: ReferralFragment,
    ) : Referral()

    data class LoadedReferral(
      val inner: ReferralFragment,
    ) : Referral()
  }

  object Error : ReferralsModel()
}
