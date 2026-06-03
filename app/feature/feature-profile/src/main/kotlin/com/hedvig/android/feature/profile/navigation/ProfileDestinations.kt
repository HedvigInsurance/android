package com.hedvig.android.feature.profile.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

@Serializable
data object ProfileKey : HedvigNavKey

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

/*
* Not saving navigation state when explicitly logging out from Profile
*/
val destinationToExcludeFromSavingState: KClass<out HedvigNavKey> = ProfileKey::class
