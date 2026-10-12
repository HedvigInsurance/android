package com.hedvig.android.experiments.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.tasks.await

/** The Remote Config values behind [FirebaseExperimentProvider], kept behind an interface so the provider is testable. */
internal interface ExperimentRemoteConfig {
  /** Fetches the latest values and makes them the active ones. Throws when the fetch fails. */
  suspend fun fetchAndActivate()

  /** The active value for [key], or an empty string when nothing has ever been activated for it. */
  fun getString(key: String): String
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
internal class FirebaseExperimentRemoteConfig(
  hedvigBuildConstants: HedvigBuildConstants,
) : ExperimentRemoteConfig {
  private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance().apply {
    if (hedvigBuildConstants.isDebug) {
      // Debug builds see console changes on the next launch; production keeps the SDK's 12 hour interval.
      setConfigSettingsAsync(
        FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build(),
      )
    }
    setDefaultsAsync(knownExperiments.associate { it.remoteConfigKey to "" })
  }

  override suspend fun fetchAndActivate() {
    remoteConfig.fetchAndActivate().await()
  }

  override fun getString(key: String): String = remoteConfig.getString(key)
}
