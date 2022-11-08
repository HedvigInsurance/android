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
import com.hedvig.hanalytics.HAnalytics
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

class HonestyPledgeBottomSheet(
  private val customActivityLaunch: ((Intent) -> Unit)? = null,
) : BottomSheetDialogFragment() {
  private val binding by viewBinding(BottomSheetHonestyPledgeBinding::bind)
  private val hAnalytics: HAnalytics by inject()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? = inflater.inflate(R.layout.bottom_sheet_honesty_pledge, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    getViewModel<HonestyPledgeViewModel>()

    binding.bottomSheetHonestyPledgeButton.setHapticClickListener {
      hAnalytics.honorPledgeConfirmed()
      startClaimsFlow()
      dismiss()
    }
  }

  private fun startClaimsFlow() {
    val intent = getClaimsFlowIntent()
    if (customActivityLaunch != null) {
      customActivityLaunch.invoke(intent)
    } else {
      startActivity(intent)
    }
  }

  private fun getClaimsFlowIntent() = EmbarkActivity.newInstance(
    requireContext(),
    "claims",
    getString(hedvig.resources.R.string.CLAIMS_HONESTY_PLEDGE_BOTTOM_SHEET_BUTTON_LABEL),
  )

  companion object {
    const val TAG = "HonestyPledgeBottomSheet"

    fun newInstance(
      customActivityLaunch: ((Intent) -> Unit)? = null,
    ): HonestyPledgeBottomSheet = HonestyPledgeBottomSheet(customActivityLaunch)
  }
}
