package com.hedvig.app.feature.claims.ui

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.odyssey.OdysseyClaimsFlowActivity
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet

suspend fun startClaimsFlow(
  featureManager: FeatureManager,
  context: Context,
  fragmentManager: FragmentManager,
  registerForResult: ((Intent) -> Unit)? = null,
  commonClaimId: String?,
) {
  if (featureManager.isFeatureEnabled(Feature.USE_ODYSSEY_CLAIM_FLOW)) {
    val intent = OdysseyClaimsFlowActivity.newInstance(
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
      .newInstance(registerForResult, commonClaimId)
      .show(fragmentManager, HonestyPledgeBottomSheet.TAG)
  }
}
