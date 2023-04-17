package com.hedvig.app.feature.offer

import android.os.Bundle
import com.hedvig.android.core.common.android.parcelable
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.referrals.ui.redeemcode.RedeemCodeBottomSheet
import giraffe.RedeemReferralCodeMutation

class OfferRedeemCodeBottomSheet : RedeemCodeBottomSheet() {

  override val quoteCartId: QuoteCartId?
    get() = arguments?.parcelable(QUOTE_CART_ID)

  override fun onRedeemSuccess(data: RedeemReferralCodeMutation.Data) {
    dismiss()
  }

  companion object {
    const val TAG = "OfferRedeemCodeDialog"
    private const val QUOTE_CART_ID = "QUOTE_CART_ID"

    fun newInstance(
      quoteCartId: QuoteCartId?,
    ) = OfferRedeemCodeBottomSheet().apply {
      if (quoteCartId != null) {
        val bundle = Bundle()
        bundle.putParcelable(QUOTE_CART_ID, quoteCartId)
        arguments = bundle
      }
    }
  }
}
