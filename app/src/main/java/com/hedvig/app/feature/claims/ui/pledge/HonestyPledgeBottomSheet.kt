package com.hedvig.app.feature.claims.ui.pledge

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.BottomSheetHonestyPledgeBinding
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.androidx.viewmodel.ext.android.getViewModel

class HonestyPledgeBottomSheet(
    private val customActivityLaunch: ((Intent) -> Unit)? = null,
) : BottomSheetDialogFragment() {
    private val binding by viewBinding(BottomSheetHonestyPledgeBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.bottom_sheet_honesty_pledge, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getViewModel<HonestyPledgeViewModel>()

        binding.bottomSheetHonestyPledgeButton.setHapticClickListener {
            startClaimsFlow()
            dismiss()
        }
    }

    private fun startClaimsFlow() {
        if (customActivityLaunch != null) {
            customActivityLaunch.invoke(getEmbarkIntent())
        } else {
            startActivity(getEmbarkIntent())
        }
    }

    private fun getEmbarkIntent(): Intent {
        return EmbarkActivity.newInstance(
            requireContext(),
            "claims",
            getString(R.string.CLAIMS_HONESTY_PLEDGE_BOTTOM_SHEET_BUTTON_LABEL)
        )
    }

    companion object {
        const val TAG = "HonestyPledgeBottomSheet"

        fun newInstance(
            customActivityLaunch: ((Intent) -> Unit)? = null,
        ): HonestyPledgeBottomSheet = HonestyPledgeBottomSheet(customActivityLaunch)
    }
}
