package com.hedvig.app.feature.claims.ui

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.odyssey.ClaimsFlowActivity
import com.hedvig.android.odyssey.ClaimsFlowActivity2
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet

suspend fun startClaimsFlow(
  featureManager: FeatureManager,
  context: Context,
  fragmentManager: FragmentManager,
  registerForResult: ((Intent) -> Unit)? = null,
  commonClaimId: String?,
) {
  if (true) {
    HonestyPledgeBottomSheet
      .newInstance(registerForResult)
      .show(fragmentManager, HonestyPledgeBottomSheet.TAG)
  } else if (featureManager.isFeatureEnabled(Feature.USE_ODYSSEY_CLAIM_FLOW)) {
    val intent = ClaimsFlowActivity.newInstance(
      context = context,
      odysseyUrl = context.getString(R.string.ODYSSEY_URL),
      commonClaimId = commonClaimId,
    )

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
