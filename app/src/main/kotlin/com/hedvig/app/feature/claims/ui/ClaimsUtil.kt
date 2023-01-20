package com.hedvig.app.feature.claims.ui

import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.hedvig.android.odyssey.ClaimsFlowActivity
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet

fun startClaimsFlow(
  fragmentManager: FragmentManager,
  registerForResult: ((Intent) -> Unit)? = null,
  commonClaimId: String?,
) {
  HonestyPledgeBottomSheet
    .newInstance(registerForResult, commonClaimId)
    .show(fragmentManager, HonestyPledgeBottomSheet.TAG)
}
