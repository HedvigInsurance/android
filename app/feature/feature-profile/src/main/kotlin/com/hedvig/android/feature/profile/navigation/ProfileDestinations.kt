package com.hedvig.android.feature.profile.navigation

import com.hedvig.android.navigation.common.DeliberateLogoutOrigin
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.common.TopLevelTabRoot
import kotlinx.serialization.Serializable

@Serializable
data object ProfileKey : HedvigNavKey, DeliberateLogoutOrigin, TopLevelTabRoot {
  override val topLevelTab: TopLevelTab = TopLevelTab.Profile
}

@Serializable
data object ContactInfoKey : HedvigNavKey

@Serializable
internal data object EurobonusKey : HedvigNavKey

@Serializable
internal data object CertificatesKey : HedvigNavKey

@Serializable
internal data object InformationKey : HedvigNavKey

@Serializable
internal data object LicensesKey : HedvigNavKey

@Serializable
internal data object SettingsKey : HedvigNavKey
