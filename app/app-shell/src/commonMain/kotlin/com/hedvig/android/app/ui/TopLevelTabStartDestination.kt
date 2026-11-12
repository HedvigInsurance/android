package com.hedvig.android.app.ui

import com.hedvig.android.feature.forever.navigation.ForeverKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.payments.navigation.PaymentsKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab

internal val TopLevelTab.startDestination: HedvigNavKey
  get() = when (this) {
    TopLevelTab.Home -> HomeKey
    TopLevelTab.Insurances -> InsurancesKey
    TopLevelTab.Forever -> ForeverKey
    TopLevelTab.Payments -> PaymentsKey
    TopLevelTab.Profile -> ProfileKey
  }
