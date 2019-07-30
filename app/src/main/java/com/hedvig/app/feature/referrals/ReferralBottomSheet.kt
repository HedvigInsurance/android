package com.hedvig.app.feature.referrals

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.referral_more_info_bottom_sheet.*

class ReferralBottomSheet : RoundedBottomSheetDialogFragment() {

    override fun getTheme() = R.style.PerilBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.referral_more_info_bottom_sheet, null)
        dialog.setContentView(view)
        arguments?.let { args ->
            dialog.referralMoreInfoParagraphOne.text = interpolateTextKey(
                getString(R.string.REFERRAL_PROGRESS_MORE_INFO_PARAGRAPH_ONE),
                "REFERRAL_VALUE" to args.getString(REFERRAL_VALUE))
            dialog.referralMoreInfoButton.setHapticClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resources.getString(R.string.REFERRAL_MORE_INFO_LINK))))
            }
        }
        return dialog
    }

    companion object {
        private const val REFERRAL_VALUE = "referral_value"

        const val TAG = "referrals_more_info"

        fun newInstance(incentive: String): ReferralBottomSheet {
            val arguments = Bundle().apply {
                putString(REFERRAL_VALUE, incentive)
            }

            return ReferralBottomSheet().also { it.arguments = arguments }
        }
    }
}
