package com.hedvig.app.feature.claims.ui

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.hedvig.android.feature.odyssey.ClaimFlowActivity
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.odyssey.search.commonclaims.SearchActivity
import com.hedvig.android.odyssey.search.groups.ClaimGroupsActivity
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet

suspend fun startClaimsFlow(
  featureManager: FeatureManager,
  context: Context,
  fragmentManager: FragmentManager,
  registerForResult: ((Intent) -> Unit)? = null,
  commonClaimId: String?,
) {
  if (featureManager.isFeatureEnabled(Feature.USE_NATIVE_CLAIMS_FLOW)) {
    val intent = if (commonClaimId != null) {
      ClaimFlowActivity.newInstance(context, commonClaimId)
    } else {
      if (featureManager.isFeatureEnabled(Feature.CLAIMS_TRIAGING)) {
        ClaimGroupsActivity.newInstance(context)
      } else {
        SearchActivity.newInstance(context)
      }
    }

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
