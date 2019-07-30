package com.hedvig.app.feature.claims.ui.pledge

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import com.hedvig.app.R
import com.hedvig.app.feature.claims.service.ClaimsTracker
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.bottom_sheet_honesty_pledge.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class HonestyPledgeBottomSheet : RoundedBottomSheetDialogFragment() {
    val tracker: ClaimsTracker by inject()

    val claimsViewModel: ClaimsViewModel by sharedViewModel()

    override fun getTheme() = R.style.NoTitleBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_honesty_pledge, null)
        dialog.setContentView(view)

        dialog.bottomSheetHonestyPledgeButton.setHapticClickListener {
            tracker.pledgeHonesty(arguments?.getString(ARGS_CLAIM_KEY))
            claimsViewModel.triggerClaimsChat {
                dismiss()
                requireActivity().startClosableChat()
            }
        }
        return dialog
    }

    companion object {
        private const val ARGS_CLAIM_KEY = "claim_key"

        fun newInstance(claimKey: String): HonestyPledgeBottomSheet {
            val arguments = Bundle().apply {
                putString(ARGS_CLAIM_KEY, claimKey)
            }

            return HonestyPledgeBottomSheet()
                .also { it.arguments = arguments }
        }
    }
}
