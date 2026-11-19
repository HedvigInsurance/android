package com.hedvig.android.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Whether auth requests carry the `Hedvig-Bankid-Sandbox` header, which has the auth service run BankID logins
 * against the BankID test environment. It can only be switched on in a non-production build.
 */
@SingleIn(AppScope::class)
@Inject
class BankIdSandboxState(
  private val dataStore: DataStore<Preferences>,
  hedvigBuildConstants: HedvigBuildConstants,
  coroutineScope: ApplicationScope,
) {
  val isAvailable: Boolean = !hedvigBuildConstants.isProduction

  // Staging and production share an application id, so a value stored by a staging build can outlive it.
  val isEnabled: StateFlow<Boolean> = if (isAvailable) {
    dataStore.data
      .map { it[key] == true }
      .stateIn(coroutineScope, SharingStarted.Eagerly, false)
  } else {
    MutableStateFlow(false)
  }

  fun headers(): Map<String, String> = if (isEnabled.value) mapOf(HEADER_NAME to "true") else emptyMap()

  suspend fun setEnabled(enabled: Boolean) {
    if (!isAvailable) return
    dataStore.edit { it[key] = enabled }
  }

  private companion object {
    const val HEADER_NAME = "Hedvig-Bankid-Sandbox"
    val key = booleanPreferencesKey("com.hedvig.android.auth.BANKID_SANDBOX_ENABLED")
  }
}
