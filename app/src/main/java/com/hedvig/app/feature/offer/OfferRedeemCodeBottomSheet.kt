package com.hedvig.app.feature.offer

import android.os.Bundle
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.referrals.ui.redeemcode.RedeemCodeBottomSheet
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class OfferRedeemCodeBottomSheet : RedeemCodeBottomSheet() {

    override val quoteCartId: QuoteCartId?
        get() = arguments?.getParcelable(QUOTE_CART_ID)

    private val offerViewModel: OfferViewModel by sharedViewModel()

    override fun onRedeemSuccess(data: RedeemReferralCodeMutation.Data) {
        dismiss()
    }

    companion object {
        const val TAG = "OfferRedeemCodeDialog"
        private const val QUOTE_CART_ID = "QUOTE_CART_ID"

        fun newInstance(
            quoteCartId: QuoteCartId?
        ) = OfferRedeemCodeBottomSheet().apply {
            if (quoteCartId != null) {
                val bundle = Bundle()
                bundle.putParcelable(QUOTE_CART_ID, quoteCartId)
                arguments = bundle
            }
        }
    }
}
