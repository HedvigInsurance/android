package com.hedvig.app.featureflags

import android.content.Context
import android.content.Intent
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.tab.ProfileModel

class FeatureFlagEntryProvider {
    fun addEntry(context: Context): ProfileModel.Row? {
        return ProfileModel.Row(
            "FeatureManager",
            "Disable or enable features",
            R.drawable.ic_info_toolbar
        ) {
            context.startActivity(Intent(context, FeatureFlagActivity::class.java))
        }
    }
}
