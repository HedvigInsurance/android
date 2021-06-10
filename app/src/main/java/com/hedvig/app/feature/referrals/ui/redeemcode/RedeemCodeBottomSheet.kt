package com.hedvig.app.feature.referrals.ui.redeemcode

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.R
import com.hedvig.app.databinding.PromotionCodeDialogBinding
import com.hedvig.app.feature.referrals.service.ReferralsTracker
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.hideKeyboard
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class RedeemCodeBottomSheet : BottomSheetDialogFragment() {
    private val model: RedeemCodeViewModel by viewModel()
    private val binding by viewBinding(PromotionCodeDialogBinding::bind)
    private val tracker: ReferralsTracker by inject()

    abstract fun onRedeemSuccess(data: RedeemReferralCodeMutation.Data)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.promotion_code_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            bottomSheetAddPromotionCodeButton.setHapticClickListener {
                redeemPromotionCode(bottomSheetAddPromotionCodeEditText.text.toString())
            }
            bottomSheetPromotionCodeTermsAndConditionLink.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.hedvig.com/invite/terms"))
                startActivity(intent)
            }
            bottomSheetAddPromotionCodeEditText.setOnEditorActionListener { v, actionId, _ ->
                return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_DONE) {
                    redeemPromotionCode(bottomSheetAddPromotionCodeEditText.text.toString())
                    view.context.hideKeyboard(v)
                    true
                } else {
                    false
                }
            }
            model.redeemCodeStatus.observe(viewLifecycleOwner) { data ->
                data?.let {
                    onRedeemSuccess(it)
                } ?: wrongPromotionCode()
            }
        }
    }

    private fun redeemPromotionCode(code: String) {
        tracker.redeemReferralCodeOverlay()
        model.redeemReferralCode(code)
    }

    private fun wrongPromotionCode() {
        binding.textField.errorIconDrawable = requireContext().compatDrawable(R.drawable.ic_warning_triangle)
        binding.textField.error = getString(R.string.REFERRAL_ERROR_MISSINGCODE_BODY)
    }
}
