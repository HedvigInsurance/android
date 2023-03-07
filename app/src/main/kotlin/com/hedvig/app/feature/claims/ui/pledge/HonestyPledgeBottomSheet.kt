package com.hedvig.app.feature.claims.ui.pledge

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.odyssey.search.SearchActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.BottomSheetHonestyPledgeBinding
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.hanalytics.HAnalytics
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

class HonestyPledgeBottomSheet(
  private val customActivityLaunch: ((Intent) -> Unit)? = null,
  private val commonClaimId: String?,
) : BottomSheetDialogFragment() {
  private val binding by viewBinding(BottomSheetHonestyPledgeBinding::bind)
  private val hAnalytics: HAnalytics by inject()
  private val featureManager: FeatureManager by inject()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? = inflater.inflate(R.layout.bottom_sheet_honesty_pledge, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    getViewModel<HonestyPledgeViewModel>()

    binding.bottomSheetHonestyPledgeButton.setHapticClickListener {
      hAnalytics.honorPledgeConfirmed()
      viewLifecycleScope.launch {
        startClaimsFlow()
      }
      dismiss()
    }
  }

  private suspend fun startClaimsFlow() {
    val intent = getClaimsFlowIntent()
    if (customActivityLaunch != null) {
      customActivityLaunch.invoke(intent)
    } else {
      startActivity(intent)
    }
  }

  private suspend fun getClaimsFlowIntent(): Intent {
    return if (featureManager.isFeatureEnabled(Feature.USE_NATIVE_CLAIMS_FLOW)) {
      return SearchActivity.newInstance(requireContext(), getString(R.string.ODYSSEY_URL))
    } else {
      EmbarkActivity.newInstance(
        requireContext(),
        "claims",
        getString(hedvig.resources.R.string.CLAIMS_HONESTY_PLEDGE_BOTTOM_SHEET_BUTTON_LABEL),
      )
    }
  }

  companion object {
    const val TAG = "HonestyPledgeBottomSheet"

    fun newInstance(
      customActivityLaunch: ((Intent) -> Unit)? = null,
      commonClaimId: String?,
    ): HonestyPledgeBottomSheet = HonestyPledgeBottomSheet(customActivityLaunch, commonClaimId)
  }
}
