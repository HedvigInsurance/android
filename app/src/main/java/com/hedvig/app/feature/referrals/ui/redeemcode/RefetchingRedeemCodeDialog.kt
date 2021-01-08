package com.hedvig.app.feature.referrals.ui.redeemcode

import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class RefetchingRedeemCodeDialog : RedeemCodeDialog() {
    private val model: PaymentViewModel by sharedViewModel()

    override fun onRedeemSuccess(data: RedeemReferralCodeMutation.Data) {
        model.load()
        dismiss()
    }

    companion object {
        val TAG = "RefetchingRedeemCodeDialog"

        fun newInstance() =
            RefetchingRedeemCodeDialog()
    }
}
