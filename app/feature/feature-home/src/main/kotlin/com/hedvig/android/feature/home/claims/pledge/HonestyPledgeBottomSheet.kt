package com.hedvig.android.feature.home.claims.pledge

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.feature.home.R
import com.hedvig.android.feature.home.databinding.BottomSheetHonestyPledgeBinding
import com.hedvig.hanalytics.HAnalytics
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

class HonestyPledgeBottomSheet(
  private val embarkClaimsFlowIntent: Intent,
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

    binding.bottomSheetHonestyPledgeButton.setOnClickListener {
      hAnalytics.honorPledgeConfirmed()
      startClaimsFlow()
      dismiss()
    }
  }

  private fun startClaimsFlow() {
    startActivity(embarkClaimsFlowIntent)
  }

  companion object {
    const val TAG = "HonestyPledgeBottomSheet"

    fun newInstance(
      embarkClaimsFlowIntent: Intent,
    ): HonestyPledgeBottomSheet = HonestyPledgeBottomSheet(embarkClaimsFlowIntent)
  }
}
