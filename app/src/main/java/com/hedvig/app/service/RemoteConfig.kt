package com.hedvig.app.service

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RemoteConfig {
    private val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        firebaseRemoteConfig.setDefaultsAsync(
            mapOf(
                "Key_Gear_Enabled" to false,
                "Campaign_Banner_SE" to false,
                "Campaign_Banner_NO" to false,
                "Campaign_Banner_DK" to false,
            )
        )
    }

    suspend fun fetch() = suspendCancellableCoroutine<RemoteConfigData> { cont ->
        firebaseRemoteConfig
            .fetchAndActivate()
            .addOnSuccessListener {
                cont.resume(RemoteConfigData.from(firebaseRemoteConfig))
            }
            .addOnFailureListener { error ->
                cont.cancel(error)
            }
    }
}

data class RemoteConfigData(
    val keyGearEnabled: Boolean,
    val seCampaignVisible: Boolean,
    val noCampaignVisible: Boolean,
    val dkCampaignVisible: Boolean,
) {
    companion object {
        fun from(firebaseRemoteConfig: FirebaseRemoteConfig) = RemoteConfigData(
            firebaseRemoteConfig.getBoolean("Key_Gear_Enabled"),
            firebaseRemoteConfig.getBoolean("Campaign_Banner_SE"),
            firebaseRemoteConfig.getBoolean("Campaign_Banner_NO"),
            firebaseRemoteConfig.getBoolean("Campaign_Banner_DK"),
        )
    }
}
