package com.hedvig.android.feature.profile.navigation

import com.hedvig.android.navigation.common.DeliberateLogoutOrigin
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data object ProfileKey : HedvigNavKey, DeliberateLogoutOrigin

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
