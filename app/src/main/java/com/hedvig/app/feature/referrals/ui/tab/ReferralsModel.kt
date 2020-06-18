package com.hedvig.app.feature.referrals.ui.tab

import com.hedvig.android.owldroid.fragment.ReferralFragment

sealed class ReferralsModel {
    object Title : ReferralsModel()
   
    sealed class Header : ReferralsModel() {
        object LoadingHeader : Header()
        object LoadedEmptyHeader : Header()
        data class LoadedHeader(
            private val todo: Unit
        ) : Header()
    }

    sealed class Code : ReferralsModel() {
        object LoadingCode : Code()
        data class LoadedCode(
            val code: String
        ) : Code()
    }

    object InvitesHeader : ReferralsModel()

    sealed class Referral : ReferralsModel() {
        object LoadingReferral : Referral()

        data class Referee(
            val inner: ReferralFragment
        ) : Referral()

        data class LoadedReferral(
            val inner: ReferralFragment
        ) : Referral()
    }

    object Error : ReferralsModel()
}
