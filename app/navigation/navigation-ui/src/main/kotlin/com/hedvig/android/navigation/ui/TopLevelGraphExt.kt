package com.hedvig.android.navigation.core

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.nav.Payments
import com.hedvig.android.core.icons.hedvig.nav.PaymentsFilled
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
    TopLevelGraph.Home -> Icons.Hedvig.HomeFilled
    TopLevelGraph.Insurances -> Icons.Hedvig.InsuranceFilled
    TopLevelGraph.Forever -> Icons.Hedvig.ForeverFilled
    TopLevelGraph.Payments -> Icons.Hedvig.PaymentsFilled
    TopLevelGraph.Profile -> Icons.Hedvig.ProfileFilled
  }
}

fun TopLevelGraph.unselectedIcon(): ImageVector {
  return when (this) {
    TopLevelGraph.Home -> Icons.Hedvig.Home
    TopLevelGraph.Insurances -> Icons.Hedvig.Insurance
    TopLevelGraph.Forever -> Icons.Hedvig.Forever
    TopLevelGraph.Payments -> Icons.Hedvig.Payments
    TopLevelGraph.Profile -> Icons.Hedvig.Profile
  }
}

fun TopLevelGraph.titleTextId(): Int {
  return when (this) {
    TopLevelGraph.Home -> R.string.TAB_HOME_TITLE
    TopLevelGraph.Insurances -> R.string.TAB_INSURANCES_TITLE
    TopLevelGraph.Forever -> R.string.TAB_REFERRALS_TITLE
    TopLevelGraph.Payments -> R.string.TAB_PAYMENTS_TITLE
    TopLevelGraph.Profile -> R.string.TAB_PROFILE_TITLE
  }
}
