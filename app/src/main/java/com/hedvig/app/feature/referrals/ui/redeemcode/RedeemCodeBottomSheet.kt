package com.hedvig.app.feature.referrals.ui.redeemcode

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.R
import com.hedvig.app.databinding.PromotionCodeDialogBinding
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.usecase.CampaignCode
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.hideKeyboard
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

abstract class RedeemCodeBottomSheet : BottomSheetDialogFragment() {

    abstract val quoteCartId: QuoteCartId?

    private val model: RedeemCodeViewModel by viewModel {
        parametersOf(quoteCartId)
    }

    private val binding by viewBinding(PromotionCodeDialogBinding::bind)

    abstract fun onRedeemSuccess(data: RedeemReferralCodeMutation.Data)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.promotion_code_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            bottomSheetAddPromotionCodeButton.setHapticClickListener {
                redeemPromotionCode(bottomSheetAddPromotionCodeEditText.text.toString().let { CampaignCode(it) })
            }
            bottomSheetPromotionCodeTermsAndConditionLink.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.hedvig.com/invite/terms"))
                startActivity(intent)
            }
            bottomSheetAddPromotionCodeEditText.setOnEditorActionListener { v, actionId, _ ->
                return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_DONE) {
                    redeemPromotionCode(bottomSheetAddPromotionCodeEditText.text.toString().let { CampaignCode(it) })
                    view.context.hideKeyboard(v)
                    true
                } else {
                    false
                }
            }

            viewLifecycleScope.launchWhenStarted {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    model.viewState.collect { state ->
                        state.errorMessage?.let {
                            wrongPromotionCode(it)
                        }
                        state.data?.let {
                            onRedeemSuccess(it)
                        }
                        state.quoteCartId?.let {
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun redeemPromotionCode(code: CampaignCode) {
        model.redeemReferralCode(code)
    }

    private fun wrongPromotionCode(errorMessage: String) {
        binding.textField.errorIconDrawable = requireContext().compatDrawable(R.drawable.ic_warning_triangle)
        binding.textField.error = errorMessage
    }
}
