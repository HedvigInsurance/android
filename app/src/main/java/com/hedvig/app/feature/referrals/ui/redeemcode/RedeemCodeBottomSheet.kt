package com.hedvig.app.feature.referrals.ui.redeemcode

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.R
import com.hedvig.app.databinding.PromotionCodeDialogBinding
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.hideKeyboard
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
            model.viewState.onEach { state ->
                state.errorMessage?.let {
                    wrongPromotionCode(it)
                }
                state.data?.let {
                    onRedeemSuccess(it)
                }
                state.quoteCartId?.let {
                    dismiss()
                }
            }.launchIn(lifecycleScope)
        }
    }

    private fun redeemPromotionCode(code: String) {
        model.redeemReferralCode(code)
    }

    private fun wrongPromotionCode(errorMessage: String) {
        binding.textField.errorIconDrawable = requireContext().compatDrawable(R.drawable.ic_warning_triangle)
        binding.textField.error = errorMessage ?: getString(R.string.offer_discount_error_alert_title)
    }
}
