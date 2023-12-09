package com.hedvig.android.navigation.core

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.nav.hedvig.Forever
import com.hedvig.android.core.icons.hedvig.nav.hedvig.ForeverFilled
import com.hedvig.android.core.icons.hedvig.nav.hedvig.Home
import com.hedvig.android.core.icons.hedvig.nav.hedvig.HomeFilled
import com.hedvig.android.core.icons.hedvig.nav.hedvig.Insurance
import com.hedvig.android.core.icons.hedvig.nav.hedvig.InsuranceFilled
import com.hedvig.android.core.icons.hedvig.nav.hedvig.Profile
import com.hedvig.android.core.icons.hedvig.nav.hedvig.ProfileFilled
import hedvig.resources.R

fun TopLevelGraph.selectedIcon(): ImageVector {
  return when (this) {
    TopLevelGraph.HOME -> Icons.Hedvig.HomeFilled
    TopLevelGraph.INSURANCE -> Icons.Hedvig.InsuranceFilled
    TopLevelGraph.FOREVER -> Icons.Hedvig.ForeverFilled
    TopLevelGraph.PROFILE -> Icons.Hedvig.ProfileFilled
  }
}

fun TopLevelGraph.unselectedIcon(): ImageVector {
  return when (this) {
    TopLevelGraph.HOME -> Icons.Hedvig.Home
    TopLevelGraph.INSURANCE -> Icons.Hedvig.Insurance
    TopLevelGraph.FOREVER -> Icons.Hedvig.Forever
    TopLevelGraph.PROFILE -> Icons.Hedvig.Profile
  }
}

fun TopLevelGraph.titleTextId(): Int {
  return when (this) {
    TopLevelGraph.HOME -> R.string.home_tab_title
    TopLevelGraph.INSURANCE -> R.string.insurances_tab_title
    TopLevelGraph.FOREVER -> R.string.TAB_REFERRALS_TITLE
    TopLevelGraph.PROFILE -> R.string.TAB_TITLE_PROFILE
  }
}
