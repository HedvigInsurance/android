package com.hedvig.app.feature.claims.ui.pledge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.BottomSheetHonestyPledgeBinding
import com.hedvig.app.feature.claims.service.ClaimsTracker
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject

class HonestyPledgeBottomSheet : BottomSheetDialogFragment() {
    private val tracker: ClaimsTracker by inject()
    private val binding by viewBinding(BottomSheetHonestyPledgeBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.bottom_sheet_honesty_pledge, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bottomSheetHonestyPledgeButton.setHapticClickListener {
            tracker.pledgeHonesty()
            viewLifecycleScope.launchWhenStarted {
                startActivity(
                    EmbarkActivity.newInstance(
                        requireContext(),
                        "claims",
                        getString(R.string.CLAIMS_HONESTY_PLEDGE_BOTTOM_SHEET_BUTTON_LABEL)
                    )
                )
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "HonestyPledgeBottomSheet"

        fun newInstance(): HonestyPledgeBottomSheet = HonestyPledgeBottomSheet()
    }
}
