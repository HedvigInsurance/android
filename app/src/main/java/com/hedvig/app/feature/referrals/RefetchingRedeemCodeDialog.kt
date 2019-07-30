package com.hedvig.app.feature.referrals

import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class RefetchingRedeemCodeDialog : RedeemCodeDialog() {
    private val profileViewModel: ProfileViewModel by sharedViewModel()

    override fun onRedeemSuccess(data: RedeemReferralCodeMutation.Data) {
        profileViewModel.updateReferralsInformation(data)
        dismiss()
    }

    companion object {
        val TAG = "RefetchingRedeemCodeDialog"

        fun newInstance() = RefetchingRedeemCodeDialog()
    }
}
