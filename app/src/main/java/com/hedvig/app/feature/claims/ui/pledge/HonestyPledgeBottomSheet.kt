package com.hedvig.app.feature.claims.ui.pledge

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.BottomSheetHonestyPledgeBinding
import com.hedvig.app.feature.claims.service.ClaimsTracker
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class HonestyPledgeBottomSheet(
    private val customActivityLaunch: ((Intent) -> Unit)? = null,
) : BottomSheetDialogFragment() {
    private val tracker: ClaimsTracker by inject()
    private val featureManager: FeatureManager by inject()
    private val claimsViewModel: ClaimsViewModel by sharedViewModel()
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
                startClaimsFlow()
                dismiss()
            }
        }
    }

    private suspend fun startClaimsFlow() {
        if (featureManager.isFeatureEnabled(Feature.EMBARK_CLAIMS)) {
            startEmbarkClaims()
        } else {
            claimsViewModel.triggerClaimsChat()
            requireActivity().startClosableChat()
        }
    }

    private fun startEmbarkClaims() {
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
