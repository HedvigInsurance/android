package com.hedvig.app.feature.referrals

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.R
import com.hedvig.app.util.extensions.hideKeyboard
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.observe
import kotlinx.android.synthetic.main.promotion_code_dialog.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

abstract class RedeemCodeDialog : DialogFragment() {

    private val referralViewModel: ReferralViewModel by viewModel()

    private val tracker: ReferralsTracker by inject()

    abstract fun onRedeemSuccess(data: RedeemReferralCodeMutation.Data)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.promotion_code_dialog, null)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setContentView(view)

        dialog.bottomSheetAddPromotionCodeButton.setHapticClickListener {
            redeemPromotionCode(dialog.bottomSheetAddPromotionCodeEditText.text.toString())
        }
        dialog.bottomSheetPromotionCodeTermsAndConditionLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.hedvig.com/invite/terms"))
            startActivity(intent)
        }
        dialog.bottomSheetAddPromotionCodeEditText.setOnEditorActionListener { v, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_DONE) {
                redeemPromotionCode(dialog.bottomSheetAddPromotionCodeEditText.text.toString())
                view.context.hideKeyboard(v)
                true
            } else {
                false
            }
        }
        referralViewModel.redeemCodeStatus.observe(this) { data ->
            data?.let {
                onRedeemSuccess(it)
            } ?: wrongPromotionCode()
        }
        return dialog
    }

    override fun onResume() {
        super.onResume()
        resetErrorState()
    }

    private fun redeemPromotionCode(code: String) {
        tracker.redeemReferralCodeOverlay()
        resetErrorState()
        referralViewModel.redeemReferralCode(code)
    }

    private fun resetErrorState() {
        dialog.bottomSheetAddPromotionCodeEditText.background =
            requireContext().getDrawable(R.drawable.background_edit_text_rounded_corners)
        dialog.bottomSheetPromotionCodeMissingCode.remove()
    }

    private fun wrongPromotionCode() {
        dialog.bottomSheetAddPromotionCodeEditText.background =
            requireContext().getDrawable(R.drawable.background_edit_text_rounded_corners_failed)
        dialog.bottomSheetPromotionCodeMissingCode.show()
    }
}
