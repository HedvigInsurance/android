package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.feature.referrals.ui.redeemcode.RedeemCodeDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class OfferRedeemCodeDialog : RedeemCodeDialog() {
    private val offerViewModel: OfferViewModel by sharedViewModel()

    override fun onRedeemSuccess(data: RedeemReferralCodeMutation.Data) {
        offerViewModel.writeDiscountToCache(data)
        dismiss()
    }

    companion object {
        const val TAG = "OfferRedeemCodeDialog"

        fun newInstance() = OfferRedeemCodeDialog()
    }
}
