package com.hedvig.app.feature.claims.ui

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet
import com.hedvig.hanalytics.HAnalytics

suspend fun startClaimsFlow(
  hAnalytics: HAnalytics,
  context: Context,
  fragmentManager: FragmentManager,
  registerForResult: ((Intent) -> Unit)? = null,
) {
  if (hAnalytics.odysseyClaims()) {
    val intent = ClaimsFlowActivity.newInstance(context)
    if (registerForResult != null) {
      registerForResult(intent)
    } else {
      context.startActivity(intent)
    }
  } else {
    HonestyPledgeBottomSheet
      .newInstance(registerForResult)
      .show(fragmentManager, HonestyPledgeBottomSheet.TAG)
  }
}
