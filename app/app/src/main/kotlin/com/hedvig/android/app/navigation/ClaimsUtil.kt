package com.hedvig.android.app.navigation

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.hedvig.android.feature.home.claims.pledge.HonestyPledgeBottomSheet
import com.hedvig.android.feature.odyssey.ClaimFlowActivity
import com.hedvig.android.feature.odyssey.search.commonclaims.SearchActivity
import com.hedvig.android.feature.odyssey.search.groups.ClaimGroupsActivity
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.app.feature.embark.ui.EmbarkActivity

suspend fun startClaimsFlow(
  featureManager: FeatureManager,
  context: Context,
  fragmentManager: FragmentManager,
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

    context.startActivity(intent)
  } else {
    HonestyPledgeBottomSheet
      .newInstance(
        embarkClaimsFlowIntent = EmbarkActivity.newInstance(
          context = context,
          storyName = "claims",
          storyTitle = context.getString(hedvig.resources.R.string.CLAIMS_HONESTY_PLEDGE_BOTTOM_SHEET_BUTTON_LABEL),
        ),
      )
      .show(fragmentManager, HonestyPledgeBottomSheet.TAG)
  }
}
