package com.hedvig.app.feature.loggedin.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.hedvig.app.feature.claims.ui.ClaimsFragment
import com.hedvig.app.feature.dashboard.ui.DashboardFragment
import com.hedvig.app.feature.keygear.ui.tab.KeyGearFragment
import com.hedvig.app.feature.profile.ui.ProfileFragment
import com.hedvig.app.feature.referrals.ReferralsFragment
import com.hedvig.app.util.extensions.byOrdinal

class TabPagerAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(page: Int): Fragment = when (byOrdinal<LoggedInTabs>(page)) {
        LoggedInTabs.DASHBOARD -> DashboardFragment()
        LoggedInTabs.CLAIMS -> ClaimsFragment()
        LoggedInTabs.KEY_GEAR -> KeyGearFragment()
        LoggedInTabs.REFERRALS -> ReferralsFragment()
        LoggedInTabs.PROFILE -> ProfileFragment()
    }

    override fun getCount() = 5
}
