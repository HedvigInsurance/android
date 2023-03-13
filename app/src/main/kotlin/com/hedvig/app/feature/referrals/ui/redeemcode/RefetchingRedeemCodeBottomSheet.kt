package com.hedvig.app.feature.referrals.ui.redeemcode

import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel
import giraffe.RedeemReferralCodeMutation
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class RefetchingRedeemCodeBottomSheet : RedeemCodeBottomSheet() {
  override val quoteCartId: QuoteCartId?
    get() = null

  private val viewModel: PaymentViewModel by activityViewModel()

  override fun onRedeemSuccess(data: RedeemReferralCodeMutation.Data) {
    viewModel.load()
    dismiss()
  }

  companion object {
    const val TAG = "RefetchingRedeemCodeDialog"

    fun newInstance() =
      RefetchingRedeemCodeBottomSheet()
  }
}
